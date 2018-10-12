(ns fm-server.app.person
  "A person in a family tree.

  ### Person

  Fields:

   - `id` unique identifier
   - `first-name`
   - `last-name`
   - `gender` either `:m` or `:f`
   - `father` ID of father (another Person object)
   - `mother` ditto
   - `spouse` ditto
   - `owner-id` ID of Account that this belongs to
  "
  (:gen-class))


(defrecord Person [id first-name last-name gender father mother spouse owner-id])

(defn pack
  "Change an person into a native Clojure data structure."
  [person]
  (into {} person))

(defn unpack
  "Convert a properly formatted Clojure data structure into an Person record."
  [data]
  (map->Person data))
