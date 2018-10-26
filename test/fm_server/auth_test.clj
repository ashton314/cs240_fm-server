(ns fm-server.auth-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]

            [storage.db.account :as account-store]
            [storage.db.auth-token :as auth-token-store]

            [fm-app.storage-protocols.account :as account-proto]
            [fm-app.storage-protocols.auth-token :as auth-token-proto]

            [fm-app.models.account :as account]
            [fm-app.models.auth-token :as auth-token]))


(deftest check-auth
  (testing "password checking and token returning"
    (let [homsar (account/unpack {:id (rand-int 1000) :username "homsar" :password "tinfoil"
                                  :gender :m})
          tok    (account/authenticate homsar "tinfoil")]
      (is (nil? (account/authenticate homsar "aluminum")) "rejected bad password")
      (is (auth-token/good? tok) "token reported as good (noop right now)")
      (is (= (:account_id tok) (:id homsar)) "IDs match"))))

(def account-db (account-store/map->AccountDbStorage
                 {:db-spec {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"}}))
(def auth-token-db (auth-token-store/map->AuthTokenDbStorage
                 {:db-spec {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"}}))

(deftest check-auth-storage
  (testing "storing tokens"
    (io/delete-file (:dbname (:db-spec account-db)) true)
    (account-store/migrate! account-db)
    (auth-token-store/migrate! auth-token-db)
    (let [homsar (account/unpack {:id (rand-int 1000) :username "homsar" :password "tinfoil"
                                  :gender :m})
          tok (conj (account/authenticate homsar "tinfoil")
                    {:id (auth-token-proto/create! auth-token-db)})]
      (auth-token-proto/save! auth-token-db (auth-token/pack tok))
      (is (= (account/pack tok)
             (dissoc (auth-token-proto/fetch auth-token-db (:token tok)) :created_at :updated_at))))))
      
