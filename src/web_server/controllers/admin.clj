(ns web-server.controllers.admin
  "Handles administrative requests"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [fm-app.services.admin :as admin]))

(defn clear-storage
  "Wipes all records from current storage system."
  [request params app]
  ((:info (:logger app)) "Clearing storage")
  (try
    (do
      (admin/clear-storage (:storage (:config app)) (:logger app))
      (-> {:message "Clear succeeded."}
          json/write-str
          ring-response/response
          (ring-response/content-type "application/json")
          (ring-response/status 200)))
    (catch Error e
      (let [message (.getMessage e)]
        ((:error (:logger app)) (str "Problem clearning storage: " message))
        (-> {:message message}
            json/write-str
            ring-response/response
            (ring-response/content-type "application/json")
            (ring-response/status 500))))))

(defn load-record
  "Wipes all records, then adds a new Person record."
  [request params app]
  (log/info (str "Admin controller got: " request))
  (log/info (str "Params: " params "\nApp: " app))
  (ring-response/response "load-record hit!"))
