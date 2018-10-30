(ns web-server.controllers.events
  "Handles events-related actions"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [fm-app.services.auth :as auth]))

(defn get-event
  "Gets all Events for the current user"
  [request params app]
  (log/info (str "Events controller got: " request))
  (log/info (str "Params: " params "\nApp: " app))
  (ring-response/response "get-events hit!"))

(defn get-all-events
  "Gets all events for a Person"
  [request params app]
  (log/info (str "Events controller got: " request))
  (log/info (str "Params: " params "\nApp: " app))
  (ring-response/response "get-all-events hit!"))
