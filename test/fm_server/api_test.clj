(ns fm-server.api-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]

            [clojure.tools.logging :as log]
            [clojure.data.json :as json]

            [web-server.router :as router]
            [fm-app.fm-app :as app]

            (fm-app.storage-protocols [account :as account-proto]
                                      [event :as event-proto]
                                      [person :as person-proto]
                                      [auth-token :as auth-token-proto])

            (storage.db [account    :as storage-account]
                        [person     :as storage-person]
                        [event      :as storage-event]
                        [auth-token :as storage-authy])))


(defn- db-spec-default
  "Returns default db-spec"
  []
  {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"})

(def conf
  "Configruation for the web server"
  {:storage {:account    (storage-account/map->AccountDbStorage {:db-spec (db-spec-default)})
             :person     (storage-person/map->PersonDbStorage {:db-spec (db-spec-default)})
             :event      (storage-event/map->EventDbStorage {:db-spec (db-spec-default)})
             :auth-token (storage-authy/map->AuthTokenDbStorage {:db-spec (db-spec-default)})}
   :server  {:port 8080}
   :routes {"/user/register" :register
            "/user/login" :login
            "/clear" :clear
            "/fill/:username/:generations" :fill
            "/load" :load
            "/person/:person_id" :get-person
            "/person" :get-all-people
            "/event/:event_id" :get-event
            "/event/" :get-all-events}})

(defn mock-request
  [req]
  (router/handle-request req (:routes conf)
                         (app/create-app conf
                                         {:info #(log/info %) :error #(log/error %)
                                          :warn #(log/warn %) :fatal #(log/fatal %)})))

(deftest register-test
  (storage-account/migrate! (:account (:storage conf)))
  (storage-authy/migrate! (:auth-token (:storage conf)))
  (storage-person/migrate! (:person (:storage conf)))
  (testing "register-a-homstar"
    (let [deets {:username (str "homsar" (rand-int 10000))
                 :password "tinfoil"
                 :first_name "homsar"
                 :last_name "scruff"
                 :email "tinfoil@hremail.com"
                 :gender :m}
          req (-> (mock/request :post "/user/register")
                  (mock/json-body deets))]
      (let [resp (mock-request req)]
        (is (= (:status resp) 201) "got ok back")
        (is (= (:id (account-proto/find-username (:account (:storage conf))
                                                 (:username deets)))
               (:account_id (auth-token-proto/fetch (:auth-token (:storage conf))
                                             (:authToken (json/read-str (:body resp) :key-fn keyword)))))))

      (let [resp (-> (mock/request :post "/user/register") (mock/json-body deets) mock-request)]
        (is (= (:status resp) 409) "caught resource already in use")))))

(deftest login-test
  (storage-account/migrate! (:account (:storage conf)))
  (storage-authy/migrate! (:auth-token (:storage conf)))
  (storage-person/migrate! (:person (:storage conf)))

  (let [deets {:username (str "homsar" (rand-int 10000))
               :password "tinfoil"
               :first_name "homsar"
               :last_name "scruffy"
               :email "tinfoil@hremail.com"
               :gender :m}
        req (-> (mock/request :post "/user/register")
                (mock/json-body deets))]

    (testing "authentication test"
      (let [register-resp (mock-request req)
            req (-> (mock/request :post "/user/login")
                    (mock/json-body {:userName (:username deets)
                                     :password (:password deets)}))
            resp (mock-request req)
            resp-body (json/read-str (:body resp) :key-fn keyword)]
        (is (= (:status resp) 200))
        (is (not-empty (:authToken resp-body)) "got some auth token back")
        (is (= (:userName resp-body) (:username deets)) "got username back")
        (is (= (:personID resp-body)
               (:personID (json/read-str (:body register-resp) :key-fn keyword)))
            "right personID back from reg")))

    (testing "failed auth test"
      (let [req (-> (mock/request :post "/user/login")
                    (mock/json-body {:userName (:username deets)
                                     :password "bad_password"}))
            resp (mock-request req)]
        (is (= (:status resp) 401) "successfully returned password error")))))
      
