(ns web-server.controllers.people
  "Handles people-related actions"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [fm-app.services.auth :as auth]))

(defn fill-ancestry
  "Populates the server's database with generated data for the specified user name."
  [request params app]
  (log/info (str "People controller got: " request))
  (log/info (str "Params: " params "\nApp: " app))
  (ring-response/response "fill-ancestry hit!"))

(defn get-person
  "Gets a Person Record specified by ID"
  [request params app]
  (log/info (str "People controller got: " request))
  (log/info (str "Params: " params "\nApp: " app))
  (ring-response/response "get-person hit!"))

(defn get-people
  "Gets all People for the current user"
  [request params app]
  (log/info (str "People controller got: " request))
  (log/info (str "Params: " params "\nApp: " app))
  (ring-response/response "get-people hit!"))
