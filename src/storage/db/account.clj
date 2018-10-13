(ns storage.db.account
  "Database backend for storing Accounts

  All these methods return *packed* objects. They should be passed to
  the Account's `unpack` function to get a vitalized record."
  (:gen-class)
  (:require [storage.utils :as util]
            [storage.account-protocol :refer :all]
            [clojure.java.jdbc :as jdbc]))

(defrecord AccountDbStorage
    [db-spec]
    AccountStorage

    (create! [self]
      (let [ret (jdbc/insert! db-spec "accounts" {:created (util/sql-now)})]
        (-> ret first seq first (get 1))))

    (save! [self packed-account]
      (jdbc/update! db-spec "accounts" packed-account ["id = ?" (:id packed-account)]))

    (fetch [self account-id]
      (first (jdbc/query db-spec ["SELECT * FROM accounts WHERE id = ?" account-id])))

    (find-username [self username]
      (first (jdbc/query db-spec ["SELECT * FROM accounts WHERE username = ?" username]))))
