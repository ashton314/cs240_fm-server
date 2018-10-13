(ns web-server.tx.request
  "Request record"
  (:gen-class))

(defrecord Request [uri query-string headers body])

(defn parse-request
  "Takes an HTTP request as a string and builds a Request record out of it."
  [http-string]
  nil)
