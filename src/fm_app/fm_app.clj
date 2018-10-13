(ns fm-app.fm-app
  "Family Sever Application"
  (:gen-class))

(defn create-app
  "App bootstrapping method---accepts map of storage mechanisms and other dependecies."
  [config]
  (println "I got some config:")
  (prn config))
