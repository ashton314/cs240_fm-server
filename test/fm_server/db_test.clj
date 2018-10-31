(ns fm-server.db-test
  (:require (storage.db [account :as account-storage]
                        [event   :as event-storage]
                        [person  :as person-storage])

            (fm-app.models [person :as person]
                           [event  :as event])

            (fm-app.storage-protocols [account :as acc-proto]
                                      [event   :as event-proto]
                                      [person  :as person-proto])

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
    (io/delete-file (:dbname (:db-spec store)) true)
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
        (acc-proto/save! store packed-account)
        (is (= packed-account (dissoc (acc-proto/fetch store (:id packed-account))
                                      :created_at :updated_at)))
        (is (= packed-account (dissoc (acc-proto/find-username store "homestar")
                                      :created_at :updated_at)))))))

(deftest person-storage-test
  (let [store (person-storage/map->PersonDbStorage
               {:db-spec {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"}})]
    (io/delete-file (:dbname (:db-spec store)) true)
    (person-storage/migrate! store)
    (let [packed-person {:id (person-proto/create! store)
                         :first_name "Bob"
                         :last_name "Ross"
                         :gender :m
                         :father nil
                         :mother nil
                         :spouse nil
                         :owner_id -1}]
      (testing "storing and retrieving a person"
        (is (= packed-person packed-person) "structs equal")
        (is (= packed-person (person/pack (person/unpack packed-person)))
            "structs equal even when one gets packed and unpacked")
        (person-proto/save! store packed-person)
        (is (= (person/unpack packed-person)
               (person/unpack (dissoc (person-proto/fetch store (:id packed-person))
                                      :created_at :updated_at))))))))
    
(deftest event-storage-test
  (let [store (event-storage/map->EventDbStorage
               {:db-spec {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"}})]
    (io/delete-file (:dbname (:db-spec store)) true)
    (event-storage/migrate! store)

    (let [packed-event {:id (event-proto/create! store)
                        :person_id 42
                        :latitude 42.0
                        :longitude -23.23
                        :country "Fomrica"
                        :city "Ender's Hollow"
                        :event_type :birth
                        :timestamp "2018-10-31"
                        :owner_id 43}]
      (testing "storing and retrieving an event"
        (event-proto/save! store packed-event)
        (is (= (event/unpack packed-event)
               (event/unpack (dissoc (event-proto/fetch store (:id packed-event))
                                     :created_at :updated_at))))
        (is (= (event/unpack packed-event)
               (event/unpack (dissoc (first (event-proto/get-user-events store (:owner_id packed-event)))
                                     :created_at :updated_at))))
        (is (= (event/unpack packed-event)
               (event/unpack (dissoc (first (event-proto/get-person-events store (:person_id packed-event)))
                                     :created_at :updated_at))))))))
                        
