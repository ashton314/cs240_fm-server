(ns fm-server.api-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]

            [clojure.tools.logging :as log]
            [clojure.data.json :as json]

            [web-server.router :as router]
            [fm-app.fm-app :as app]

            (fm-app.models [person :as person]
                           [account :as account])

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
      
(deftest clear-test
  (storage-account/migrate! (:account (:storage conf)))
  (storage-authy/migrate! (:auth-token (:storage conf)))
  (storage-person/migrate! (:person (:storage conf)))
  (storage-event/migrate! (:event (:storage conf)))
  (let [reg_resp (-> (mock/request :post "/user/register")
                     (mock/json-body
                      {:username (str "homsar" (rand-int 10000)) :password "tinfoil"
                       :first_name "homsar" :last_name "scruffy"
                       :email "tinfoil@hremail.com" :gender :m})
                     mock-request)]
    (testing "checking that I got a person and stuff in the db"
      (is (not (nil? (account-proto/find-username (:account (:storage conf))
                                                  (:userName (json/read-str (:body reg_resp) :key-fn keyword)))))))

    (testing "clear functionality"
      (let [clear-resp (mock-request (mock/request :post "/clear"))]
        (is (= 200 (:status clear-resp)))
        (is (= "Clear succeeded." (:message (json/read-str (:body clear-resp) :key-fn keyword))))))

    (testing "checking that I got nothing in the db now"
      (is (nil? (account-proto/find-username (:account (:storage conf))
                                             (:userName (json/read-str (:body reg_resp) :key-fn keyword))))))))

(deftest fetch-test
  (let [kirk (str "kirk" (rand-int 100000))
        spock (str "spock" (rand-int 100000))
        kirk-data (json/read-str (:body (-> (mock/request :post "/user/register")
                                            (mock/json-body {:username kirk :password "enterprise" :first_name "James" :last_name "Kirk" :email "kirk@enterprise.ufp" :gender :m})
                                            mock-request)) :key-fn keyword)
        spock-data (json/read-str (:body (-> (mock/request :post "/user/register")
                                             (mock/json-body {:username spock :password "vulcan" :first_name "Mr." :last_name "Spock" :email "spock@enterprise.ufp" :gender :m})
                                             mock-request)) :key-fn keyword)]

    (testing "fetch mr. spock's person"
      (let [person-resp (mock-request (mock/header (mock/request :get (str "/people/" (:personID spock-data))) "Authorization" (:authToken spock-data)))]
        (is (= 200 (:status person-resp)))
        (is (= (person/unpack {:descendant spock
                               :personID (:personID spock-data)
                               :firstName "Mr."
                               :lastName "Spock"
                               :gender :m})
               (person/unpack (select-keys (json/read-str (:body person-resp) :key-fn keyword)
                                           [:descendant :personID :firstName :lastName :gender]))))))

    (testing "fetch mr. spock's people"
      (let [people-resp (mock-request (mock/header (mock/request :get "/people") "Authorization" (:authToken spock-data)))]
        (is (= 200 (:status people-resp)))
        (is (= 31 (count (:data (json/read-str (:body people-resp) :key-fn keyword)))))))))
                  

;; (deftest fill-test
;;   (let [reg_resp (-> (mock/request :post "/user/register")
;;                      (mock/json-body
;;                       {:username (str "ender" (rand-int 10000)) :password "the_giant"
;;                        :first_name "Ender" :last_name "Wiggin"
;;                        :email "ewiggin@battleschool.if" :gender :m})
;;                      mock-request)]
;;     (testing "trying to fill"
      
