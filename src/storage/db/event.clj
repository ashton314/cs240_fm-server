(ns storage.db.event
  "Database backend for storing Events

  All these methods return *packed* objects. They should be passed to
  the Event's `unpack` function to get a vitalized record."
  (:gen-class)
  (:require [storage.utils :as util]
            [fm-app.storage-protocols.event :refer :all]
            [clojure.java.jdbc :as jdbc]))

(defrecord EventDbStorage
    [db-spec]
  EventStorage

  (create! [self]
    (let [ret (jdbc/insert! db-spec "events" {:created_at (util/sql-now)})]
      (-> ret first seq first (get 1))))

  (save! [self packed-event]
    (jdbc/update! db-spec "events" packed-event ["id = ?" (:id packed-event)]))

  (fetch [self event-id]
    (first (jdbc/query db-spec ["SELECT * FROM events WHERE id = ?" event-id])))

  (get-user-events [self account]
    (jdbc/query db-spec ["SELECT * FROM events WHERE owner_id = ?" account]))

  (get-person-events [self person]
    (jdbc/query db-spec ["SELECT * FROM events WHERE person_id = ?" person]))

  (drop-by-owner! [self owner-id]
    (jdbc/execute! db-spec ["DELETE FROM events WHERE owner_id = ?" owner-id]))

  (drop-all! [self]
      (jdbc/execute! db-spec ["DELETE FROM events"])))
  
(defn migrate!
  "Creates the events table if it doesn't exist."
  [db-store]
  (jdbc/db-do-commands (:db-spec db-store)
                       "CREATE TABLE IF NOT EXISTS events(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT DEFAULT(DATETIME('now')),
  updated_at TEXT DEFAULT(DATETIME('now')),
  event_type TEXT,
  latitude REAL,
  longitude REAL,
  country TEXT,
  city TEXT,
  timestamp TEXT,
  person_id INTEGER,
  owner_id INTEGER
);"))
