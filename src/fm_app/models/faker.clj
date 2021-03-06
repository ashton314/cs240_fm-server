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

(defn gen-location
  "Generates a name"
  []
  (rand-nth (:data (json/read-str (slurp "resources/faker_data/locations.json") :key-fn keyword))))

(defn gen-event-type
  "Generates random event type"
  []
  (rand-nth [:birth :baptism :christening :marriage :death]))

(defn gen-year
  "Generates random year in the past 400 years"
  []
  (+ 1600 (rand-int 400)))

(defn gen-month
  "Generates a month number"
  []
  (+ 1 (rand-int 13)))

(defn gen-day
  "Generates a safe day (i.e. 1-28)"
  []
  (+ 1 (rand-int 29)))

(defn gen-timestamp
  "Generates a timestamp in the form YYYY-MM-DD"
  []
  (format "%04d-%02d-%02d" (gen-year) (gen-month) (gen-day)))

(defn gen-timestamp-from-year
  "Generates a timestamp in the form YYYY-MM-DD, with a specified year."
  [year]
  (format "%04d-%02d-%02d" year (gen-month) (gen-day)))
