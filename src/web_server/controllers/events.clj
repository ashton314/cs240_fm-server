(ns web-server.controllers.events
  "Handles events-related actions"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [fm-app.models.event :as event]
            (fm-app.services [auth :as auth]
                             [events :as event-service])))

(defn get-event
  "Gets all Events for the current user"
  [request params app]
  (->
   (if-let [account (auth/find-account-by-token (:storage (:config app)) (:logger app)
                                                ((:headers request) "authorization"))]
     (try
       (let [event (event-service/get-event (:event (:storage (:config app))) (:logger app)
                                    (:event_id params))]
         (if (and event (event/owned-by? event account))
           (-> {:descendant (:username account)
                :eventID (:id event)
                :personID (:person_id event)
                :eventType (name (:event_type event))
                :latitude (:latitude event)
                :longitude (:longitude event)
                :country (:country event)
                :city (:city event)
                :year (get (re-find #"^(\d+)-" (:timestamp event)) 1)}
               json/write-str
               ring-response/response
               (ring-response/status 200))
           (-> {:message "Not found."}  ; event not found for this person
               json/write-str
               ring-response/response
               (ring-response/status 404))))
       (catch Error e
         (let [message (.getMessage e)]
           ((:error (:logger app)) (str "Problem finding event: " message))
           ((:error (:logger app)) (str "Request object: " request))
           (-> {:message "Server error."}
               json/write-str
               ring-response/response
               (ring-response/status 500)))))
     (-> {:message "Not authorized."}
         json/write-str
         ring-response/response
         (ring-response/status 500)))
   (ring-response/content-type "application/json")))

(defn get-all-events
  "Gets all events for a Person"
  [request params app]
(->
   (if-let [account (auth/find-account-by-token (:storage (:config app)) (:logger app)
                                                ((:headers request) "authorization"))]
     (try
       (if-let [events (event-service/get-events (:event (:storage (:config app))) (:logger app)
                                    (:id account))]
         (-> {:data (map #(hash-map :descendant (:username account)
                                    :eventID (:id %)
                                    :personID (:person_id %)
                                    :eventType (name (:event_type %))
                                    :latitude (:latitude %)
                                    :longitude (:longitude %)
                                    :country (:country %)
                                    :city (:city %)
                                    :year (get (re-find #"^(\d+)-" (:timestamp %)) 1))
                         events)}
             json/write-str
             ring-response/response
             (ring-response/status 200))
         (-> {:message "Not found."}  ; event not found for this person
             json/write-str
             ring-response/response
             (ring-response/status 404)))
       (catch Error e
         (let [message (.getMessage e)]
           ((:error (:logger app)) (str "Problem finding events: " message))
           ((:error (:logger app)) (str "Request object: " request))
           (-> {:message "Server error."}
               json/write-str
               ring-response/response
               (ring-response/status 500)))))
     (-> {:message "Not authorized."}
         json/write-str
         ring-response/response
         (ring-response/status 500)))
   (ring-response/content-type "application/json")))
