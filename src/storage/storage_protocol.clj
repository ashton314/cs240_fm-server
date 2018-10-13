(ns storage.storage-protocol
  "Interfaces that storage methods must implement"
  (:gen-class))

(defprotocol AccountStorage
  "Storage protocol for an Account record."
  (create [self] "Returns a new ID for an Account record.")
  (save [self packed-account] "Saves a packed Account.")
  (fetch [self account-id] "Returns a packed Account record."))

(defprotocol PersonStorage
  "Storage protocol for a Person record."
  (create [self] "Returns a new ID for an Person record.")
  (save [self packed-person] "Saves a packed Person.")
  (fetch [self person-id] "Returns a packed Person record.")
  (fetch-all [self field value] "Finds all Person records where their fields match a given value."))

(defprotocol AuthTokenStorage
  "Storage protocol for an AuthToken record."
  (create [self] "Returns a new ID for an AuthToken record.")
  (save [self packed-auth-token] "Saves a packed AuthToken.")
  (fetch [self auth-token-id] "Returns a packed AuthToken record."))

(defprotocol EventStorage
  "Storage protocol for an Event record."
  (create [self] "Returns a new ID for an Event record.")
  (save [self packed-event] "Saves a packed Event.")
  (fetch [self event-id] "Returns a packed Event record."))
