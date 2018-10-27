(ns cl-interface
  "Runs the app from a command-line interface"
  (:gen-class)
  (:require [fm-app.fm-app :as app]
            (storage.db  [account    :as storage-account]
                         [person     :as storage-person]
                         [event     :as storage-event]
                         [auth-token :as storage-authy])))


(defn- db-spec-default
  "Returns default db-spec"
  []
  {:dbtype "sqlite" :dbname "/tmp/fm-server-test.db"})

(def conf
  "Configruation for the web server"
  {:storage {:account    (storage-account/map->AccountDbStorage {:db-spec (db-spec-default)})
             :person     (storage-person/map->PersonDbStorage {:db-spec (db-spec-default)})
             :event      (storage-event/map->EventDbStorage {:db-spec (db-spec-default)})
             :auth-token (storage-authy/map->AuthTokenDbStorage {:db-spec (db-spec-default)})}
   :server  {:port 8080}
   :routes "routing spec goes here"})

(defn -main
  "Fire off the web server. Main method---port may be listed on command
  line. Otherwise the default 8080 will be used."
  [& args]
  (app/create-app conf))
