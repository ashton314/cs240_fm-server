(ns web-server.router
  "Routing utilities for the web server"
  (:gen-class)
  (:require [clout.core :as clout]
            [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            (web-server.controllers [register :as c-register]
                                    [admin :as c-admin]
                                    [events :as c-events]
                                    [login :as c-login]
                                    [people :as c-people])))

(defn handle-request
  "Takes a Request record and a route spec and dispatches to the proper controller."
  [request routing-spec application]
  (let [matches (keep #(if-let [result (clout/route-matches % request)]
                                    [% result])
                      (keys routing-spec))]
    (log/info (str "Request: " request))
    (log/info (str "matches: " [(first matches) (rest matches)]))
    (if (empty? matches)
      (ring-response/not-found (str "The URI " (:uri request) " did not match any routes."))
      ;; TODO: add a try/catch to throw 500 errors
      (-> ({:register c-register/register-account
            :login c-login/authenticate
            :clear c-admin/clear-storage
            :fill nil
            :load nil
            :get-person nil
            :get-all-people nil
            :get-event nil
            :get-all-events nil} (routing-spec (ffirst matches)))
          (apply [request (get (first matches) 1) application])))))

(defn parse-uri
  "Parse a URI string and a routing pattern."
  [uri route-pattern]
  nil)
