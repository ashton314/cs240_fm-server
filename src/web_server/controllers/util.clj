(ns web-server.controllers.util
  "General utilities for controllers (e.g. validation)"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.data.json :as json]))

(defmacro validate
  "Given a vector of clauses like [condition error-code message], tests
  each condition. If condition is false, return an ring error message
  with the status and error message specified. Otherwise return false."
  ([] false)
  ([& clauses]
   (let [[test err-code message] (first clauses)]
     `(if ~test (validate ~@(rest clauses))
          (-> {:message ~message}
              json/write-str
              ring-response/bad-request
              (ring-response/status ~err-code))))))
