(ns fm-server.models.auth-token
  "Authorization token"
  (:gen-class))

(defrecord AuthToken [id account-id token expires])

(defn generate-token
  "Creates a new Authentication Token."
  ([account-id]
   (unpack {:account-id account-id :token (generate-random-string)}))
  ([account-id expires]
   (unpack {:account-id account-id :expires expires :token (generate-random-string)})))

(defn pack
  "Change an AuthToken into a native Clojure data structure."
  [token]
  (into {} token))

(defn unpack
  "Convert a properly formatted Clojure data structure into an AuthToken record."
  [data]
  (map->AuthToken data))

(defn- generate-random-string
  "Creates a random string by calling `java.util.UUID.randomUUID`."
  []
  (.toString (java.util.UUID/randomUUID))
              
