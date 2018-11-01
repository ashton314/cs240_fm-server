(ns fm-server.api-test
  (:require [clojure.test :refer :all]
            [clojure.set :as set]
            [clojure.java.io :as io]
            [ring.mock.request :as mock]

            [clojure.tools.logging :as log]
            [clojure.data.json :as json]

            [web-server.router :as router]
            [fm-app.fm-app :as app]

            (fm-app.services [auth :as auth-service])

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
            "/fill/:username" :fill-4-gens
            "/load" :load
            "/person/:person_id" :get-person
            "/person" :get-all-people
            "/event/:event_id" :get-event
            "/event" :get-all-events}})

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
    (let [deets {:userName (str "homsar" (rand-int 10000))
                 :password "tinfoil"
                 :firstName "homsar"
                 :lastName "scruff"
                 :email "tinfoil@hremail.com"
                 :gender :m}
          req (-> (mock/request :post "/user/register")
                  (mock/json-body deets))]
      (let [resp (mock-request req)]
        (is (= (:status resp) 201) "got ok back")
        (is (= (:id (account-proto/find-username (:account (:storage conf))
                                                 (:userName deets)))
               (:owner_id (auth-token-proto/fetch (:auth-token (:storage conf))
                                             (:authToken (json/read-str (:body resp) :key-fn keyword)))))))

      (let [resp (-> (mock/request :post "/user/register") (mock/json-body deets) mock-request)]
        (is (= (:status resp) 409) "caught resource already in use")))))

(deftest login-test
  (storage-account/migrate! (:account (:storage conf)))
  (storage-authy/migrate! (:auth-token (:storage conf)))
  (storage-person/migrate! (:person (:storage conf)))

  (let [deets {:userName (str "homsar" (rand-int 10000))
               :password "tinfoil"
               :firstName "homsar"
               :lastName "scruffy"
               :email "tinfoil@hremail.com"
               :gender :m}
        req (-> (mock/request :post "/user/register")
                (mock/json-body deets))]

    (testing "authentication test"
      (let [register-resp (mock-request req)
            req (-> (mock/request :post "/user/login")
                    (mock/json-body {:userName (:userName deets)
                                     :password (:password deets)}))
            resp (mock-request req)
            resp-body (json/read-str (:body resp) :key-fn keyword)]
        (is (= (:status resp) 200))
        (is (not-empty (:authToken resp-body)) "got some auth token back")
        (is (= (:userName resp-body) (:userName deets)) "got username back")
        (is (= (:personID resp-body)
               (:personID (json/read-str (:body register-resp) :key-fn keyword)))
            "right personID back from reg")))

    (testing "failed auth test"
      (let [req (-> (mock/request :post "/user/login")
                    (mock/json-body {:userName (:userName deets)
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
                      {:userName (str "homsar" (rand-int 10000)) :password "tinfoil"
                       :firstName "homsar" :lastName "scruffy"
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
  (storage-account/migrate! (:account (:storage conf)))
  (storage-authy/migrate! (:auth-token (:storage conf)))
  (storage-person/migrate! (:person (:storage conf)))
  (storage-event/migrate! (:event (:storage conf)))
  (let [kirk (str "kirk" (rand-int 100000))
        spock (str "spock" (rand-int 100000))
        kirk-data (json/read-str (:body (-> (mock/request :post "/user/register")
                                            (mock/json-body {:userName kirk :password "enterprise" :firstName "James" :lastName "Kirk" :email "kirk@enterprise.ufp" :gender :m})
                                            mock-request)) :key-fn keyword)
        spock-data (json/read-str (:body (-> (mock/request :post "/user/register")
                                             (mock/json-body {:userName spock :password "vulcan" :firstName "Mr." :lastName "Spock" :email "spock@enterprise.ufp" :gender :m})
                                             mock-request)) :key-fn keyword)]

    (testing "fetching mr. spock's account by token"
      (is (not (nil? (auth-service/find-account (:account (:storage conf)) nil spock))))
      (is (= (auth-service/find-account (:account (:storage conf)) nil spock)
             (auth-service/find-account-by-token (:storage conf) nil (:authToken spock-data)))))

    (testing "fetch mr. spock's person"
      (let [person-resp (mock-request (mock/header (mock/request :get (str "/person/" (:personID spock-data))) "Authorization" (:authToken spock-data)))]
        (is (= 200 (:status person-resp)))
        (is (= (person/unpack {:descendant spock
                               :personID (:personID spock-data)
                               :firstName "Mr."
                               :lastName "Spock"
                               :gender :m})
               (person/unpack (select-keys (json/read-str (:body person-resp) :key-fn keyword)
                                           [:descendant :personID :firstName :lastName :gender]))))
        (is (not (nil? (:father (json/read-str (:body person-resp) :key-fn keyword)))) "spock has a father")))

    (testing "fetch mr. spock's people"
      (let [people-resp (mock-request (mock/header (mock/request :get "/person") "Authorization" (:authToken spock-data)))]
        (is (= 200 (:status people-resp)))
        (is (= 31 (count (:data (json/read-str (:body people-resp) :key-fn keyword)))))))

    (testing "fetching mr. spock's events"
      (let [event-resp (mock-request (mock/header (mock/request :get "/event") "Authorization" (:authToken spock-data)))]
        (is (= 200 (:status event-resp)))
        (is (= 76 (count (:data (json/read-str (:body event-resp) :key-fn keyword)))))))
    
    (testing "fetching an event for mr. spock"
      (let [all-events-resp (mock-request (mock/header (mock/request :get "/event") "Authorization" (:authToken spock-data)))
            all-events (:data (json/read-str (:body all-events-resp) :key-fn keyword))
            event-resp (mock-request (mock/header (mock/request :get (str "/event/" (:eventID (first all-events)))) "Authorization" (:authToken spock-data)))]
        (is (= 200 (:status event-resp)))))

    (testing "kirk tries to access with a bad auth token"
      (let [event-resp (mock-request (mock/header (mock/request :get "/event") "Authorization" "not-an-auth-token"))]
        (is (= 401 (:status event-resp)))))))

(deftest fill-test
  (let [ender (str "ender" (rand-int 10000))
        reg-resp (-> (mock/request :post "/user/register")
                     (mock/json-body
                      {:userName ender :password "the_giant"
                       :firstName "Ender" :lastName "Wiggin"
                       :email "ewiggin@battleschool.if" :gender :m})
                     mock-request)
        ender-data (json/read-str (:body reg-resp) :key-fn keyword)]

    (testing "registration went well"
      (is (= (:status reg-resp) 201)))

    (let [family-resp (mock-request (mock/header (mock/request :get "/person")
                                                 "Authorization" (:authToken ender-data)))
          family (:data (json/read-str (:body family-resp) :key-fn keyword))

          events-resp (mock-request (mock/header (mock/request :get "/event")
                                                 "Authorization" (:authToken ender-data)))
          events (:data (json/read-str (:body events-resp) :key-fn keyword))]

      (testing "fill should wipe out old data"

        (let [fill-resp (mock-request (mock/request :post (str "/fill/" ender "/4")))]
          (is (= (:status fill-resp) 200))
          (is (= (:message (json/read-str (:body fill-resp) :key-fn keyword)) "Successfully added 31 persons and 76 events to the database.")))

        (let [new-auth-req (-> (mock/request :post "/user/login")
                               (mock/json-body {:userName ender
                                                :password "the_giant"})
                               mock-request)
              new-auth-token (:authToken (json/read-str (:body new-auth-req) :key-fn keyword))
              new-family-resp (mock-request (mock/header (mock/request :get "/person")
                                                         "Authorization" new-auth-token))
              new-family (:data (json/read-str (:body new-family-resp) :key-fn keyword))

              new-events-resp (mock-request (mock/header (mock/request :get "/event")
                                                         "Authorization" new-auth-token))
              new-events (:data (json/read-str (:body new-events-resp) :key-fn keyword))]
          (is (= (count new-family) 31))
          (is (= (count new-events) 76))
          (is (= (count (set/intersection (into #{} new-family) (into #{} family))) 0))
          (is (= (count (set/intersection (into #{} new-events) (into #{} events))) 0)))))))

(deftest load-test
  (let [accounts [{:userName "bones" :password "doc" :firstName "Leonard" :lastName "McCoy" :gender "m" :email "mccoy@starfleet.ifp" :personID "bones-self"}
                  {:userName "jim"   :password "captn" :firstName "James" :lastName "Kirk" :gender "m" :email "j.kirk@starfleet.ifp" :personID "jim-self"}]
        people   [{:descendant "jim"   :personID "jim-self"     :firstName "James"   :lastName "Kirk"  :gender "m" :father "jim-father"   :mother "jim-mother"}
                  {:descendant "bones" :personID "bones-self"   :firstName "Leonard" :lastName "McCoy" :gender "m" :father "bones-father" :mother "bones-mother"}
                  {:descendant "jim"   :personID "jim-father"   :firstName "George"  :lastName "Kirk"  :gender "m" :spouse "jim-mother"}
                  {:descendant "jim"   :personID "jim-mother"   :firstName "Winona"  :lastName "Kirk"  :gender "f" :spouse "jim-father"}
                  {:descendant "bones" :personID "bones-father" :firstName "David"   :lastName "McCoy" :gender "m" :spouse "bones-mother"}
                  {:descendant "bones" :personID "bones-mother" :firstName "Abigail" :lastName "McCoy" :gender "f" :spouse "bones-father"}]
        events [{:descendant "jim" :eventID "kirk-born" :personID "jim-self" :eventType "birth" :latitude 42.0 :longitude -11.17 :country "Far Edge" :city "U.S.S. Kelvin" :year "2234"}
                {:descendant "jim" :eventID "kirk-death" :personID "jim-father" :eventType "death" :latitude 42.0 :longitude -11.17 :country "Far Edge" :city "U.S.S. Kelvin" :year "2234"}]]
    (let [resp (-> (mock/request :post "/load")
                   (mock/json-body {:users accounts :persons people :events events})
                   mock-request)]
      (testing "fill test"
        (let [resp-body (json/read-str (:body resp) :key-fn keyword)]
          (is (= (:message resp-body) "Successfully added 2 users, 6 persons, and 2 events to the database."))
          (is (= #{:jim-self :bones-self :jim-father :jim-mother :bones-father :bones-mother} (into #{} (keys (:persons resp-body)))) "map returned has correct keys")
          (is (= #{:kirk-born :kirk-death} (into #{} (keys (:events resp-body)))) "map returned has correct keys"))))))

