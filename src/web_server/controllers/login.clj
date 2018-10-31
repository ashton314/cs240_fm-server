(ns web-server.controllers.login
  "Handles login requests"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [ring.util.request :as ring-request]
            [clojure.tools.logging :as log]
            [web-server.controllers.util :refer :all]
            [clojure.data.json :as json]
            [fm-app.services.auth :as auth]))

(defn authenticate
  "Takes a Request and attempts to authenticate the user."
  [request params app]
  (let [req-body (json/read-str (ring-request/body-string request) :key-fn keyword)]
    (if-let [error-resp (validate
                         [(not-any? nil? (map #(req-body %) [:userName :password]))
                          400 "Missing parameters"])]
      error-resp
      (do
        ((:info (:logger app)) (str "Authentication request for " (:userName req-body)))
        (let [token (auth/authenticate (:account (:storage (:config app))) (:logger app)
                                       (:userName req-body) (:password req-body))
              account (auth/find-account (:account (:storage (:config app))) (:logger app) (:userName req-body))]
          (if token
            (-> {:authToken (:token token)
                 :userName (:userName req-body)
                 :personID (:root_person account)}
                json/write-str
                ring-response/response
                (ring-response/content-type "application/json")
                (ring-response/status 200))
            (-> "Invalid username/password pair"
                ring-response/response
                (ring-response/status 401))))))))
            
