(ns web-server.core
  "Primary event-loop services for the web server"
  (:gen-class)
  (:require [web-server.router :as router]))

(defn listen
  "Main event loop---listens on a given port."
  [config]
  nil)

