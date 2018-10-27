(ns web-server.controllers.login
  "Handles login requests"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [fm-app.services.auth :as auth]))

(defn authenticate
  "Takes a Request and attempts to authenticate the user."
  [request params app]
  (log/info (str "Authenticat controller got it: " request))
  (ring-response/response "I got hit!")) ;here I'd call auth/authenticate
