(ns web-server.tx.response
  "Response record"
  (:gen-class))

(defrecord Response [status reason-phrase headers body])

(defn serialize
  "Packs a Response record into a string suitable for the HTTP protocol."
  [response]
  nil)
