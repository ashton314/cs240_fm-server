(ns fm-server.core
  "Family Sever Application"
  (:gen-class))

(defn create-app
  "App bootstrapping method---accepts map of storage mechanisms and other dependecies."
  [config]
  (println "I got me some config")
  (prn config))
