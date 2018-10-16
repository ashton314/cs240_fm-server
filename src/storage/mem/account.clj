(ns storage.mem.account
  "In-memory backend for storing Accounts

  All these methods return *packed* objects. They should be passed to
  the Account's `unpack` function to get a vitalized record."
  (:gen-class)
  (:require [storage.utils :as util]
            [storage.protocols.account :refer :all]
            [mount.core :as mount]
            [clojure.java.jdbc :as jdbc]))

(defrecord AccountMemStorage
    [db-connection]
  AccountStorage

  (create! [self]
    (let [ret (jdbc/insert! db-connection "accounts" {:created_at (util/sql-now)})]
      (-> ret first seq first (get 1))))

  (save! [self packed-account]
    (jdbc/update! db-connection "accounts" packed-account ["id = ?" (:id packed-account)]))

  (fetch [self account-id]
    (first (jdbc/query db-connection ["SELECT * FROM accounts WHERE id = ?" account-id])))

  (find-username [self username]
    (first (jdbc/query db-connection ["SELECT * FROM accounts WHERE username = ?" username]))))

(defn migrate!
  "Creates the accounts table if it doesn't exist."
  [db-store]
  (jdbc/db-do-commands (:db-connection db-store)
                       "CREATE TABLE IF NOT EXISTS accounts(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT DEFAULT(DATETIME('now')),
  updated_at TEXT DEFAULT(DATETIME('now')),
  username TEXT,
  password TEXT,
  first_name TEXT,
  last_name TEXT,
  email TEXT,
  gender TEXT,
  root_person INTEGER
);"))


(defn create-storage!
  []
  ;; (util/start-conn)
  (map->AccountMemStorage {:db-connection util/mem-socket}))
