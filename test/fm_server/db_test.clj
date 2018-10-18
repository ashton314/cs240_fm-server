(ns fm-server.db-test
  (:require [storage.db.account :as account-storage]
            [fm-app.storage-protocols.account :as acc-proto]
            [clojure.test :refer :all]
            [clojure.java.io :as io]))

(deftest db-test
  (let [store (account-storage/map->AccountDbStorage
               {:db-spec {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"}})]
    (testing "creation"
      (is (account-storage/migrate! store))
      (is (> (acc-proto/create! store) 0)))))

(deftest crud-test
  (let [store (account-storage/map->AccountDbStorage
               {:db-spec {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"}})]
    (io/delete-file (:dbname (:db-spec store)))
    (account-storage/migrate! store)
    (let [packed-account {:id (acc-proto/create! store)
                          :first_name "Homestar"
                          :last_name "Runner"
                          :username "homestar"
                          :password "marzipan"
                          :email "hr@email.com"
                          :gender "male"
                          :root_person -1}]
      (testing "create retrieve update"
        (prn (acc-proto/save! store packed-account))
        (is (= packed-account (dissoc (acc-proto/fetch store (:id packed-account))
                                      :created_at :updated_at)))
        (is (= packed-account (dissoc (acc-proto/find-username store "homestar")
                                      :created_at :updated_at)))))))
