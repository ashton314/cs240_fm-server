(ns storage.db.auth-token
  "Database backend for storing Authentication Tokens"
  (:gen-class)
  (:require [storage.utils :as util]
            [storage.protocols.auth-token :refer :all]
            [clojure.java.jdbc :as jdbc]))

(defrecord AuthTokenDbStorage
    [db-spec]
  AuthTokenStorage

  (create! [self] nil)
  (save! [self packed-auth-token] nil)
  (fetch [self token-id] nil))
