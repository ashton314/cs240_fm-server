(ns fm-app.fm-app
  "Family Sever Application"
  (:gen-class)
  (:require (fm-app.services [admin :as admin-service]
                             [auth :as auth-service]
                             [events :as events-service]
                             [people :as people-service])))

(defn create-app
  "App bootstrapping method---accepts map of storage mechanisms and other dependecies."
  [config logger] ; logger should look like {:info #() :error #() :warn #() :fatal #()}
  (assert (= #{:info :warn :error :fatal} (into #{} (keys logger)))
          "Application needs logging handles for info, warn, error, and fatal")
  {:config config
   :logger logger})
