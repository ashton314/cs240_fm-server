(ns web-server
  "Primary web server class"
  (:gen-class)
  (:require [fm-app.fm-app :as app]
            [clojure.data.json :as json]
            [web-server.core :as ws-core]
            [clojure.tools.logging :as log]
            (storage.db [account    :as storage-account]
                        [person     :as storage-person]
                        [event      :as storage-event]
                        [auth-token :as storage-authy])))


(defn- db-spec-default
  "Returns default db-spec"
  []
  {:dbtype "sqlite" :dbname "/tmp/fm-server.db"})

(def conf
  "Configruation for the web server"
  {:storage {:account    (storage-account/map->AccountDbStorage {:db-spec (db-spec-default)})
             :person     (storage-person/map->PersonDbStorage {:db-spec (db-spec-default)})
             :event      (storage-event/map->EventDbStorage {:db-spec (db-spec-default)})
             :auth-token (storage-authy/map->AuthTokenDbStorage {:db-spec (db-spec-default)})}
   :server  {:port 8080}
   :routes {"/user/register" :register
            "/user/register/" :register
            "/user/login" :login
            "/user/login/" :login
            "/clear" :clear
            "/clear/" :clear
            "/fill/:username/:generations" :fill
            "/fill/:username/:generations/" :fill
            "/fill/:username" :fill-4-gens
            "/fill/:username/" :fill-4-gens
            "/load" :load
            "/load/" :load
            "/person/:person_id" :get-person
            "/person/:person_id/" :get-person
            "/person" :get-all-people
            "/person/" :get-all-people
            "/event/:event_id" :get-event
            "/event/:event_id/" :get-event
            "/event" :get-all-events
            "/event/" :get-all-events
            "/" :home-page
            "/index.html" :home-page
            "/css/:filename" :css
            "/favicon.ico" :favicon}})

(defn -main
  "Fire off the web server. Main method---port may be listed on command
  line. Otherwise the default 8080 will be used."
  [& args]
  (storage-account/migrate! (:account (:storage conf)))
  (storage-authy/migrate! (:auth-token (:storage conf)))
  (storage-person/migrate! (:person (:storage conf)))
  (storage-event/migrate! (:event (:storage conf)))
  (let [config (if args (conj conf {:server {:port (read-string (first args))}}) conf)]
    (ws-core/listen config
                    (app/create-app config
                                    {:info #(log/info %) :error #(log/error %)
                                     :warn #(log/warn %) :fatal #(log/fatal %)}))))
