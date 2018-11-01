(ns web-server.controllers.people
  "Handles people-related actions"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            (fm-app.services [auth :as auth]
                             [admin :as admin]
                             [people :as people])))

(defn fill-ancestry
  "Populates the server's database with generated data for the specified username."
  [request params app]
  ((:info (:logger app))
   (str "Request do fill user " (:username params) " " (:generations params) " generations"))
  (->
   (if-let [account (auth/find-account (:account (:storage (:config app))) (:logger app)
                                       (:username params))]
     (try
       (do
         (admin/clear-account account (:storage (:config app)) (:logger app))
         (let [{people :people
                events :events} (admin/fill-account account (read-string (:generations params))
                                                    (:storage (:config app)) (:logger app))]
           (-> {:message (str "Successfully added " (count people) " persons and " (count events)
                              " events to the database.")}
               json/write-str
               ring-response/response
               (ring-response/status 200))))
       (catch Error e
         (let [message (.getMessage e)]
           ((:error (:logger app)) (str "Problem finding event: " message))
           ((:error (:logger app)) (str "Request object: " request))
           (-> {:message "Server error."}
               json/write-str
               ring-response/response
               (ring-response/status 500)))))
     (-> {:message "Username not found."}
         json/write-str
         ring-response/response
         (ring-response/status 404)))
   (ring-response/content-type "application/json")))

(defn fill-ancestry-default
  "Populates the default 4 generations for a specified username."
  [request params app]
  (fill-ancestry request (conj params {:generations "4"}) app))

(defn get-person
  "Gets a Person Record specified by ID"
  [request params app]
  (if-let [account (auth/find-account-by-token (:storage (:config app)) (:logger app)
                                               ((:headers request) "authorization"))]
    (try
      (let [person (people/get-person (:person (:storage (:config app))) (:logger app)
                                         (:person_id params))]
        (if (and person (= (:owner_id person) (:id account)))
          (-> {:personID (:id person)     ; person found
               :firstName (:first_name person)
               :lastName (:last_name person)
               :gender (:gender person)
               :father (:father person)
               :mother (:mother person)
               :spouse (:spouse person)
               :descendant (:username account)}
              json/write-str
              ring-response/response
              (ring-response/content-type "application/json")
              (ring-response/status 200))
          (-> {:message "Not found."}     ; person not found, but auth good
              json/write-str
              ring-response/response
              (ring-response/content-type "application/json")
              (ring-response/status 404))))
      (catch Error e
        (let [message (.getMessage e)]
          ((:error (:logger app)) (str "Problem finding person: " message))
          ((:error (:logger app)) (str "Request object: " request))
          (-> {:message "Server error."}
              json/write-str
              ring-response/response
              (ring-response/content-type "application/json")
              (ring-response/status 500)))))
    (-> {:message "Not authorized."}
        json/write-str
        ring-response/response
        (ring-response/content-type "application/json")
        (ring-response/status 500))))
            
    

(defn get-people
  "Gets all People for the current user"
  [request params app]
  (if-let [account (auth/find-account-by-token (:storage (:config app)) (:logger app)
                                               ((:headers request) "authorization"))]
    (try
      (if-let [people (people/lookup-person (:person (:storage (:config app))) (:logger app)
                                            :owner_id (:id account))]
        (-> {:data (map #(hash-map :personID (:id %)     ; person found
                                   :firstName (:first_name %)
                                   :lastName (:last_name %)
                                   :gender (:gender %)
                                   :father (:father %)
                                   :mother (:mother %)
                                   :spouse (:spouse %)
                                   :descendant (:username account))
                        people)}
            json/write-str
            ring-response/response
            (ring-response/content-type "application/json")
            (ring-response/status 200))
        (-> {:message "Not found."}     ; person not found, but auth good
            json/write-str
            ring-response/response
            (ring-response/content-type "application/json")
            (ring-response/status 404)))
      (catch Error e
        (let [message (.getMessage e)]
          ((:error (:logger app)) (str "Problem finding people: " message))
          ((:error (:logger app)) (str "Request object: " request))
          (-> {:message "Server error."}
              json/write-str
              ring-response/response
              (ring-response/content-type "application/json")
              (ring-response/status 500)))))
    (-> {:message "Not authorized."}
        json/write-str
        ring-response/response
        (ring-response/content-type "application/json")
        (ring-response/status 500))))
