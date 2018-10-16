(ns storage.protocols.auth-token
  "Interfaces that AuthToken storage mechanisms must implement"
  (:gen-class))

(defprotocol AuthTokenStorage
  "Storage protocol for an AuthToken record."
  (create! [self] "Returns a new ID for an AuthToken record.")
  (save! [self packed-auth-token] "Saves a packed AuthToken.")
  (fetch [self auth-token-id] "Returns a packed AuthToken record."))