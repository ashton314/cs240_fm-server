(ns fm-app.storage-protocols.event
  "Interfaces that Event storage mechanisms must implement"
  (:gen-class))

(defprotocol EventStorage
  "Storage protocol for an Event record."
  (create! [self] "Returns a new ID for an Event record.")
  (save! [self packed-event] "Saves a packed Event.")
  (fetch [self event-id] "Returns a packed Event record.")
  (get-user-events [self account] "Gets Events for a given user.")
  (get-person-events [self person] "Gets Events for a given person."))
