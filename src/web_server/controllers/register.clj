(ns web-server.controllers.register
  "Handles registration requests"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [fm-app.services.auth :as auth]))

(defn register-account
  "Create a new account"
  [request params app]
  (log/info (str "Register controller got: " request))
  (log/info (str "Params: " params "\nApp: " app))
  (ring-response/response "register-account hit!"))
