(ns web-server
  (:gen-class)
  (:require [fm-server.core :as app]))

(def conf
  "Configruation for the web server"
  {:storage {:account    "New account storage system here"
             :person     "New person storage system here"
             :auth-token "Authy storage"}})

(defn -main
  "Fire off the web server"
  [& args]
  (app/create-app conf))
