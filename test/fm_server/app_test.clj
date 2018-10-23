(ns fm-server.app-test
  (:require [clojure.test :refer :all]
            [fm-app.storage-protocols.account :as account-proto]
            [storage.db.account :as account-store]
            [storage.db.person :as person-store]
            [mount.core :as mount]
            [clojure.java.io :as io]
            [fm-app.models.account :as account]
            [fm-app.models.person :as person])
  (:gen-class))

(deftest smoke
  (testing "smoke test"
    (is (= 1 1))))

(def account-db (account-store/map->AccountDbStorage
                 {:db-spec {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"}}))

(def person-db (person-store/map->PersonDbStorage
                 {:db-spec {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"}}))

(deftest account-crud-test
  (let [store account-db]
        (io/delete-file (:dbname (:db-spec store)) true)
    (account-store/migrate! store)
    (let [packed-account {:id (account-proto/create! store)
                          :first_name "Homestar"
                          :last_name "Runner"
                          :username "homestar"
                          :password "marzipan"
                          :email "hr@email.com"
                          :gender "male"
                          :root_person -1}]
      (testing "create retrieve update"
        (account-proto/save! store packed-account)
        (is (= packed-account (dissoc (account-proto/fetch store (:id packed-account))
                                      :created_at :updated_at)))
        (is (= packed-account (dissoc (account-proto/find-username store "homestar")
                                      :created_at :updated_at)))))))

(deftest account-model-test
  (let [store account-db
        homsar (account/unpack {:first_name "Homsar" :username "homsar" :password "tinfoil"})]
    (testing "passwords"
      (is (account/correct-password? homsar "tinfoil"))
      (is (not (account/correct-password? homsar "tinfoli")))
      (is (not (account/authenticate homsar "tinfoli")))
      (let [tok (account/authenticate homsar "tinfoil")]
        (is tok)
        (is (account/good-token? homsar tok))))))

(deftest person-model-test
  (let [store person-db
        alice (person/unpack {:first_name "Alice" :last_name "Public" :gender :f})
        bob   (person/unpack {:first_name "Bob" :last_name "Doe" :gender :m})
        eve   (person/unpack {:first_name "Eve" :last_name "Sdropper" :gender :f})]
    (testing "test person models"
      (let [[alice1 bob1] (person/marry alice bob)]
        (is (= (:spouse alice1) (:id bob)))
        (is (= (:spouse bob1) (:id alice))))
      (let [did-test (atom false)]
        (try (person/marry alice eve)
             (catch Error e (swap! did-test (constantly true))))
        (is (= @did-test true))))))
