(ns storage.db.auth-token
  "Database backend for storing Authentication Tokens"
  (:gen-class)
  (:require [storage.utils :as util]
            [fm-app.storage-protocols.auth-token :refer :all]
            [clojure.java.jdbc :as jdbc]))

(defrecord AuthTokenDbStorage
    [db-spec]
  AuthTokenStorage

  (create! [self]
    (let [ret (jdbc/insert! db-spec "auth_tokens" {:created_at (util/sql-now)})]
      (-> ret first seq first (get 1))))

  (save! [self packed-auth-token]
    (jdbc/update! db-spec "auth_tokens" packed-auth-token ["id = ?" (:id packed-auth-token)]))

  (fetch [self token-string]
    (first (jdbc/query db-spec ["SELECT * FROM auth_tokens WHERE token = ?" token-string]))))

(defn migrate!
  "Creates the accounts table if it doesn't exist."
  [db-store]
  (jdbc/db-do-commands (:db-spec db-store)
                       "CREATE TABLE IF NOT EXISTS auth_tokens(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT DEFAULT(DATETIME('now')),
  updated_at TEXT DEFAULT(DATETIME('now')),
  account_id INTEGER,
  token TEXT,
  expires TEXT
);"))
