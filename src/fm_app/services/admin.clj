(ns fm-app.services.admin
  "Administrative services"
  (:gen-class))

(defn clear-storage
  "Wipes all records from storage."
  [storage-map]
  nil)

(defn load-person
  "Wipes all records, then adds a new Person record."
  [storage person]
  (do (clear-storage storage)
      #_(let [new-id (person-storage/create storage)]
        (person/pack (conj person {:id new-id}))))
  nil)
