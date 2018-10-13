(ns storage.mem.event
  "In-memory backend for storing Events

  All these methods return *packed* objects. They should be passed to
  the Event's `unpack` function to get a vitalized record."
  (:gen-class))

(defn save-event!
  "Takes packed Event and saves it."
  [event]
  nil)

(defn new-event!
  "Reserves a slot in the database and returns an ID for a new Event record."
  []
  nil)

(defn get-user-events
  "Retrieves Events relating to People owned by an Account."
  [account]
  nil)

(defn get-person-events
  "Retrieves Events belonging to a Person."
  [person]
  nil)
