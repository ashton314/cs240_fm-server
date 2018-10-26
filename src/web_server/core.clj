(ns web-server.core
  "Primary event-loop services for the web server"
  (:gen-class)
  (:require [web-server.router :as router]
            [ring.adapter.jetty :as jetty]))

(defn listen
  "Main event loop---listens on a given port."
  [config application]
  (jetty/run-jetty #(router/handle-request % (:routes config) application)
                   (:server config)))

