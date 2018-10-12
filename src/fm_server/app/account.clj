(ns fm-server.app.account
  "Handles user creation, authentication, etc.

  ### Account

  Fields:

   - `id` Unique ID
   - `username`
   - `password` Stored as a hash
   - `first-name`
   - `last-name`
   - `email`
   - `gender` Either `:m` or `:f`
   - `root-person` ID of Person entity this account maps to
  "
  (:gen-class))

(defrecord Account [id username password first-name last-name email gender root-person])

(defn set-password
  "Sets an account password"
  [account new-passwd]
  (conj account {:password new-passwd}))

(defn correct-password?
  "Checks a password on an account"
  [account passwd]
  (= (:password account) passwd))

(defn pack
  "Change an account into a native Clojure data structure."
  [account]
  (into {} account))

(defn unpack
  "Convert a properly formatted Clojure data structure into an Account record."
  [data]
  (map->Account data))
