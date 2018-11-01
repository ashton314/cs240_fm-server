(ns web-server.controllers.admin
  "Handles administrative requests"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [ring.util.request :as ring-request]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [fm-app.services.admin :as admin]
            [clojure.set :as set]
            [web-server.controllers.util :refer :all]))

(defn clear-storage
  "Wipes all records from current storage system."
  [request params app]
  ((:info (:logger app)) "Clearing storage")
  (try
    (do
      (admin/clear-storage (:storage (:config app)) (:logger app))
      (-> {:message "Clear succeeded."}
          json/write-str
          ring-response/response
          (ring-response/content-type "application/json")
          (ring-response/status 200)))
    (catch Error e
      (let [message (.getMessage e)]
        ((:error (:logger app)) (str "Problem clearning storage: " message))
        (-> {:message message}
            json/write-str
            ring-response/response
            (ring-response/content-type "application/json")
            (ring-response/status 500))))))

(defn load-record
  "Wipes all records, then adds a new Person record."
  [request params app]
  ((:info (:logger app)) "Loading new data")
  (let [resp (clear-storage request params app)
        req-body (json/read-str (ring-request/body-string request) :key-fn keyword)]
    (->
     (if (= 200 (:status resp))
       (if-let [error-resp (validate
                            [(not-any? nil? (map #(req-body %) [:users :persons :events])) 400 "Missing parameters: need 'users' 'persons' 'events'"]
                            [(every? true? (map (fn [user] (not-any? nil? (map #(user %) [:userName :password :email :firstName :lastName :gender :personID]))) (:users req-body)))
                             400 "Bad user: need parameters 'userName' 'password' 'email' 'firstName' 'lastName' 'gender' 'personID'"]
                            [(every? true? (map (fn [person] (not-any? nil? (map #(person %) [:descendant :personID :firstName :lastName :gender]))) (:persons req-body)))
                             400 "Bad person: need parameters 'descendant' 'personID' 'firstName' 'lastName' 'gender'"]
                            [(every? true? (map (fn [event] (not-any? nil? (map #(event %) [:descendant :eventID :personID :latitude :longitude :country :city :eventType :year]))) (:events req-body)))
                             400 "Bad event: need parameters 'descendant' 'eventID' 'personID' 'latitude' 'longitude' 'country' 'city' 'eventType' 'year'"])]
         error-resp
         (try
           (let [{people :people events :events accounts :accounts people-map :people_map event-map :event_map}
                 (admin/load-records (:storage (:config app)) (:logger app)
                                     (map #(set/rename-keys % {:userName :username :firstName :first_name :lastName :last_name :personID :root_person}) (:users req-body))
                                     (map #(set/rename-keys % {:personID :id :firstName :first_name :lastName :last_name :descendant :owner_id}) (:persons req-body))
                                     (map #(set/rename-keys % {:personID :person_id :descendant :owner_id :eventID :id :eventType :event_type}) (:events req-body)))]
             (-> {:message (str "Successfully added " (count accounts) " users, " (count people) " persons, and " (count events) " events to the database.")
                  :persons people-map :events event-map}
                 json/write-str
                 ring-response/response
                 (ring-response/status 201)))
           (catch Error e
             ((:error (:logger app)) (str "Error loading people: " (.getMessage e)))
             (-> {:message (str "Error loading database: " (.getMessage e))}
                 json/write-str
                 ring-response/response
                 (ring-response/status 500)))))
       resp)
     (ring-response/content-type "application/json"))))
