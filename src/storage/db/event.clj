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

  (create! [self] nil)

  (save! [self packed-event] nil)

  (fetch [self event-id] nil)

  (get-user-events [self account] nil)

  (get-person-events [self person] nil))
  
(defn migrate!
  "Creates the events table if it doesn't exist."
  [db-store]
  (jdbc/db-do-commands (:db-spec db-store)
                       "CREATE TABLE IF NOT EXISTS eventss(
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
