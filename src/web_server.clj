(ns web-server
  "Primary web server class"
  (:gen-class)
  (:require [fm-app.fm-app :as app]
            [clojure.data.json :as json]
            (storage.db [account    :as storage-account]
                        [person     :as storage-person]
                        [event      :as storage-event]
                        [auth-token :as storage-authy])))


(def conf
  "Configruation for the web server"
  {:storage {:account    "Account storage object here"
             :person     "New person storage system here"
             :auth-token "Authy storage"}
   :server  {:port 8080}})

(defn -main
  "Fire off the web server. Main method---port may be listed on command
  line. Otherwise the default 8080 will be used."
  [& args]
  (app/create-app conf))
