(ns web-server.controllers.register
  "Handles registration requests"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [ring.util.request :as ring-request]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [fm-app.services.admin :as admin]))

(defn register-account
  "Create a new account"
  [request params app]
  (try
    (let [resp (admin/register (select-keys (json/read-str (ring-request/body-string request) :key-fn keyword)
                                 [:username :password :first_name :last_name :email :gender])
                    (:storage (:config app)) (:logger app))]
      (-> {:authToken (:token (:auth_token resp))
           :userName (:username resp)
           :personID (:person_id resp)}
          json/write-str
          ring-response/response
          (ring-response/header "Location" (str "/user/" (:username resp)))
          (ring-response/content-type "application/json")
          (ring-response/status 201)))
    (catch Error e
      (ring-response/bad-request (.getMessage e)))))
