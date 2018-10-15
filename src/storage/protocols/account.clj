(ns storage.protocols.account
  "Interface Account storage mechanisms must implement"
  (:gen-class))

(defprotocol AccountStorage
  "Storage protocol for an Account record."
  (create! [self] "Returns a new ID for an Account record.")
  (save! [self packed-account] "Saves a packed Account.")
  (fetch [self account-id] "Returns a packed Account record by ID.")
  (find-username [self username] "Returns packed Account by Username"))
