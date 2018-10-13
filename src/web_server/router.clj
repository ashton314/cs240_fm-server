(ns web-server.router
  "Routing utilities for the web server"
  (:gen-class))

(defn handle-request
  "Takes a Request record and a route spec and dispatches to the proper handler."
  [request routing-spec]
  nil)

(defn parse-uri
  "Parse a URI string and a routing pattern."
  [uri route-pattern]
  nil)
