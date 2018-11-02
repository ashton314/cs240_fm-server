(ns fm-app.models.event
  "Life Event model

  ### Event

  Fields:

   - `id` unique identifier
   - `person_id` ID of Person record this event belongs to
   - `latitude`
   - `longitude`
   - `country` 
   - `city`
   - `event_type` Type of event (e.g. marriage, birth, death, etc.)
   - `timestamp` When this event occured
   - `owner_id` ID of Account that this belongs to
  "
  (:gen-class))

(defrecord Event [id person_id latitude longitude country city event_type timestamp owner_id])

(defn belongs-to?
  "Returns whether or not this Event belongs to a given Person."
  [event person]
  (= (:person_id event) (:id person)))

(defn owned-by?
  "Returns whether or not this Event is owned by a given Account."
  [event account]
  (= (:owner_id event) (:id account)))

(defn pack
  "Change an event into a native Clojure data structure."
  [event]
  (into {} event))

(defn unpack
  "Convert a properly formatted Clojure data structure into an Event record."
  [data]
  (map->Event (conj data {:event_type (cond (contains? #{:birth ":birth" "birth"} (:event_type data)) :birth
                                            (contains? #{:baptism ":baptism" "baptism"} (:event_type data)) :baptism
                                            (contains? #{:christening ":christening" "christening"} (:event_type data)) :christening
                                            (contains? #{:marriage ":marriage" "marriage"} (:event_type data)) :marriage
                                            (contains? #{:death ":death" "death"} (:event_type data)) :death
                                            :else (:event_type data))})))
