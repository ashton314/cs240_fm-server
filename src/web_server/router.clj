(ns web-server.router
  "Routing utilities for the web server"
  (:gen-class)
  (:require [clout.core :as clout]
            [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            (web-server.controllers [register :as c-register]
                                    [admin :as c-admin]
                                    [events :as c-events]
                                    [login :as c-login]
                                    [resources :as c-resources]
                                    [people :as c-people])))

(defn handle-request
  "Takes a Request record and a route spec and dispatches to the proper controller."
  [request routing-spec application]
  (let [matches (keep #(if-let [result (clout/route-matches % request)]
                                    [% result])
                      (keys routing-spec))]
    (if (empty? matches)
      (ring-response/not-found (json/write-str {:message (str "The URI " (:uri request) " did not match any routes.")}))
      ;; TODO: add a try/catch to throw 500 errors
      (let [args [request (get (first matches) 1) application]]
        (-> ({:register c-register/register-account
              :login c-login/authenticate
              :clear c-admin/clear-storage
              :fill c-people/fill-ancestry
              :fill-4-gens c-people/fill-ancestry-default
              :load c-admin/load-record
              :get-person c-people/get-person
              :get-all-people c-people/get-people
              :get-event c-events/get-event
              :get-all-events c-events/get-all-events
              :home-page c-resources/render
              :css c-resources/render-css
              :favicon c-resources/render-favicon} (routing-spec (ffirst matches)))
            (apply args))))))

(defn parse-uri
  "Parse a URI string and a routing pattern."
  [uri route-pattern]
  nil)
