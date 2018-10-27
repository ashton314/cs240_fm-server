(ns web-server
  "Primary web server class"
  (:gen-class)
  (:require [fm-app.fm-app :as app]
            [clojure.data.json :as json]
            [web-server.core :as ws-core]
            (storage.db [account    :as storage-account]
                        [person     :as storage-person]
                        [event      :as storage-event]
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
   :routes {"/user/register" :register
            "/user/login" :login
            "/clear" :clear
            "/fill/:username/:generations" :fill
            "/load" :load
            "/person/:person_id" :get-person
            "/person" :get-all-people
            "/event/:event_id" :get-event
            "/event/" :get-all-events}})

(defn -main
  "Fire off the web server. Main method---port may be listed on command
  line. Otherwise the default 8080 will be used."
  [& args]
  (ws-core/listen conf (app/create-app conf)))
