(ns web-server.controllers.register
  "Handles registration requests"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [ring.util.request :as ring-request]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [fm-app.services.admin :as admin]))

(defmacro validate
  ([] false)
  ([& clauses]
   (let [[test err-code message] (first clauses)]
     `(if ~test (validate ~@(rest clauses))
          (-> ~message
              ring-response/bad-request
              (ring-response/status ~err-code))))))

(defn register-account
  "Create a new account"
  [request params app]
  (let [account-details (select-keys (json/read-str (ring-request/body-string request) :key-fn keyword)
                                     [:username :password :first_name :last_name :email :gender])]

    (if-let [error-message (validate
                            [(= #{:username :password :first_name :last_name :email :gender}
                                (into #{} (keys account-details)))
                             400 "Missing args"]
                            [(some #{(:gender account-details)} #{:m :f "m" "f" ":m" ":f"})
                             400 "Gender must be 'm' or 'f'"]
                            [(not-any? nil? (vals account-details))
                             400 "Details may not be null"])]
      error-message
      (try
        (let [resp (admin/register account-details
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
          (let [message (.getMessage e)]
            (if (re-find #"^Assert failed: Username already in use" message)
              (->
               (ring-response/response "Username unavailable")
               (ring-response/status 409))
              (->
               (ring-response/response (str "Server error: " (.getMessage e)))
               (ring-response/status 500)))))))))
