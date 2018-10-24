(ns fm-app.models.faker
  "Generate fake data for the various models. Just returns maps."

  (:require [clojure.data.json :as json])
  (:gen-class))

(defn gen-name
  "Generates a name"
  [gender placement]
  (let [src-file (if (= placement :first)
                   (if (= gender :m)
                     "resources/faker_data/mnames.json"
                     "resources/faker_data/fnames.json")
                   "resources/faker_data/snames.json")]
    (rand-nth (:data (json/read-str (slurp src-file) :key-fn keyword)))))
