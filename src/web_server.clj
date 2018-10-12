(ns web-server
  "Primary web server class"
  (:gen-class)
  (:require [fm-server.fm-server :as app]
            (storage.db [account    :as storage-account]
                        [person     :as storage-person]
                        [auth-token :as storage-authy])))


(def conf
  "Configruation for the web server"
  {:storage {:account    {:save storage-account/save-account! ;TODO: wrap this in an object
                          :new  storage-account/new-account!
                          :find storage-account/find-username}
             :person     "New person storage system here"
             :auth-token "Authy storage"}
   :server  {:port 8080}})

(defn -main
  "Fire off the web server. Main method---port may be listed on command
  line. Otherwise the default 8080 will be used."
  [& args]
  (app/create-app conf))
