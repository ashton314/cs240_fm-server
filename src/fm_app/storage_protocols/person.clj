(ns fm-app.storage-protocols.person
  "Interfaces that Person storage mechanisms must implement"
  (:gen-class))

(defprotocol PersonStorage
  "Storage protocol for a Person record."
  (create! [self] "Returns a new ID for an Person record.")
  (save! [self packed-person] "Saves a packed Person.")
  (fetch [self person-id] "Returns a packed Person record.")
  (fetch-all [self field value] "Finds all Person records where their fields match a given value."))
