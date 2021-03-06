(ns web-server.controllers.register
  "Handles registration requests"
  (:gen-class)
  (:require [web-server.controllers.util :refer :all]
            [ring.util.response :as ring-response]
            [ring.util.request :as ring-request]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [clojure.set :as set]
            [fm-app.services.admin :as admin]))

(defn register-account
  "Create a new account"
  [request params app]
  (let [account-details (select-keys (json/read-str (ring-request/body-string request) :key-fn keyword)
                                     [:userName :password :firstName :lastName :email :gender])]

    (if-let [error-message (validate    ; MACROS!!!!
                            [(= #{:userName :password :firstName :lastName :email :gender}
                                (into #{} (keys account-details)))
                             400 "Missing args: need userName password firstName lastName email gender"]
                            [(some #{(:gender account-details)} #{:m :f "m" "f" ":m" ":f"})
                             400 "Gender must be 'm' or 'f'"]
                            [(not-any? nil? (vals account-details))
                             400 "Details may not be null"])]
      error-message
      (try
        (let [resp (admin/register (set/rename-keys account-details {:userName :username :firstName :first_name :lastName :last_name})
                                   (:storage (:config app)) (:logger app))]
          (-> {:authToken (:token (:auth_token resp))
               :userName (:username resp)
               :personID (str (:person_id resp))}
              json/write-str
              ring-response/response
              (ring-response/header "Location" (str "/user/" (:username resp)))
              (ring-response/content-type "application/json")
              (ring-response/status 201)))
        (catch Error e
          (let [message (.getMessage e)]
            (if (re-find #"^Assert failed: Username already in use" message)
              (->
               {:message "Username unavailable"}
               json/write-str
               ring-response/response
               (ring-response/content-type "application/json")
               (ring-response/status 409))
              (->
               {:message (str "Server error: " (.getMessage e))}
               (ring-response/content-type "application/json")
               ring-response/response
               (ring-response/status 500)))))))))
