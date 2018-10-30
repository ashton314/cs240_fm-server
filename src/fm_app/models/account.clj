(ns fm-app.models.account
  "Handles user creation, authentication, etc.

  ### Account

  Fields:

   - `id` Unique ID
   - `username`
   - `password` Stored as a hash
   - `first_name`
   - `last_name`
   - `email`
   - `gender` Either `:m` or `:f`
   - `root_person` ID of Person entity this account maps to
  "
  (:gen-class)
  (:require [fm-app.models.auth-token :as auth-token]))

(defrecord Account [id username password first_name last_name email gender root_person])

(defn set-password
  "Sets an account password."
  [account new-passwd]
  (conj account {:password new-passwd}))

(defn correct-password?
  "Checks a password on an account."
  [account passwd]
  (= (:password account) passwd))

(defn authenticate
  "Checks a password and returns a token if it's good."
  [account passwd]
  (if (correct-password? account passwd)
    (auth-token/generate-token (:id account))
    nil))

(defn good-token?
  "Checks if a token is indeed owned by this model and if it's still a good token."
  [account token]
  (and (= (:account-id token) (:id account))
       (auth-token/good? token)))

(defn pack
  "Change an account into a native Clojure data structure."
  [account]
  (into {} account))

(defn unpack
  "Convert a properly formatted Clojure data structure into an Account record."
  [data]
  (map->Account data))
