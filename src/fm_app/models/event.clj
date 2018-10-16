(ns fm-app.models.event
  "Life Event model

  ### Event

  Fields:

   - `id` unique identifier
   - `person-id` ID of Person record this event belongs to
   - `owner-id` ID of Account that this belongs to
  "
  (:gen-class))

(defrecord Event [id person_id latitude longitude country city event_type timestamp owner_id])

(defn belongs-to?
  "Returns whether or not this Event belongs to a given Person."
  [event person]
  (= (:person-id event) (:id person)))

(defn owned-by?
  "Returns whether or not this Event is owned by a given Account."
  [event account]
  (= (:owner-id event) (:id account)))

(defn pack
  "Change an event into a native Clojure data structure."
  [event]
  (into {} event))

(defn unpack
  "Convert a properly formatted Clojure data structure into an Event record."
  [data]
  (map->Event data))
