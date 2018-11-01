(ns fm-app.services.events
  "Event service"
  (:gen-class)
  (:require (fm-app.models [event :as event]
                           [auth-token :as auth-token])
            (fm-app.storage-protocols [auth-token :as token-proto]
                                      [event :as event-proto])))
            

(defn get-event
  "Get an Event by ID"
  [storage logger id]
  (if-let [event (event-proto/fetch storage id)]
    (event/unpack event)))

(defn get-events
  "Gets all Events associated with a particular account"
  [storage logger account-id]
  (if-let [events (event-proto/get-user-events storage account-id)]
    (map event/unpack events)))

(defn get-persons-events
  "Gets all events for a Person"
  [storage person]
  nil)
