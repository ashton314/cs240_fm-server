(ns cl-interface
  "Runs the app from a command-line interface"
  (:gen-class)
  (:require [fm-app.fm-app :as app]
            (storage.mem [account    :as storage-account]
                         [person     :as storage-person]
                         [event     :as storage-event]
                         [auth-token :as storage-authy])))


(def conf
  "Configruation for the command-line interface."
  {:storage {:account    {:save storage-account/save-account! ;TODO: wrap this in an object
                          :new  storage-account/new-account!
                          :find storage-account/find-username}
             :person     "New person storage system here"
             :auth-token "Authy storage"}
   :server  {:port 8080}})

(defn -main
  "Begin the command-line interface to the Family Map program."
  [& args]
  (app/create-app conf))
