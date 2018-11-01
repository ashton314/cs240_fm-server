(ns storage.db.person
  "Database backend for storing Person records"
  (:gen-class)
  (:require [storage.utils :as util]
            [fm-app.storage-protocols.person :refer :all]
            [clojure.java.jdbc :as jdbc]))

(defrecord PersonDbStorage
    [db-spec]
  PersonStorage

  (create! [self]
    (let [new_id (jdbc/insert! db-spec "people" {:created_at (util/sql-now)})]
      (-> new_id first seq first (get 1))))

  (save! [self packed-person]
    (jdbc/update! db-spec "people" packed-person ["id = ?" (:id packed-person)]))

  (fetch [self person-id]
      (first (jdbc/query db-spec ["SELECT * FROM people WHERE id = ?" person-id])))

  (fetch-all [self field value]
    (jdbc/find-by-keys db-spec "people" {field value}))

  (drop-by-owner! [self owner-id]
    (jdbc/execute! db-spec ["DELETE FROM people WHERE owner_id = ?" owner-id]))

  (drop-all! [self]
      (jdbc/execute! db-spec ["DELETE FROM people"])))

(defn migrate!
  "Creates people table if it doesn't exist."
  [db-store]
  (jdbc/db-do-commands (:db-spec db-store)
                       "CREATE TABLE IF NOT EXISTS people(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT DEFAULT(DATETIME('now')),
  updated_at TEXT DEFAULT(DATETIME('now')),
  first_name TEXT,
  last_name TEXT,
  gender TEXT,
  father INTEGER,
  mother INTEGER,
  spouse INTEGER,
  owner_id INTEGER
);"))
