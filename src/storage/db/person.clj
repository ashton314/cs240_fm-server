(ns storage.db.person
  "Database backend for storing Person records"
  (:gen-class)
  (:require [storage.utils :as util]
            [fm-app.storage-protocols.person :refer :all]
            [clojure.java.jdbc :as jdbc]))

(defrecord PersonDbStorage
    [db-spec]
  PersonStorage

  (create! [self] nil)
  (save! [self packed-person] nil)
  (fetch [self person-id] nil)
  (fetch-all [self field value] nil))  
