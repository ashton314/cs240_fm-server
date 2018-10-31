(ns web-server.controllers.admin
  "Handles administrative requests"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [fm-app.services.auth :as auth]))

(defn clear-storage
  "Wipes all records from current storage system."
  [request params app]
  (log/info (str "Admin controller got: " request))
  (log/info (str "Params: " params "\nApp: " app))
  (-> {:message "Unimplemented"}
      json/write-str
      ring-response/response
      (ring-response/content-type "application/json")
      (ring-response/status 501)))

(defn load-record
  "Wipes all records, then adds a new Person record."
  [request params app]
  (log/info (str "Admin controller got: " request))
  (log/info (str "Params: " params "\nApp: " app))
  (ring-response/response "load-record hit!"))
