(ns fm-app.models.auth-token
  "Authorization token"
  (:gen-class))

(defrecord AuthToken [id owner_id token expires])

(defn pack
  "Change an AuthToken into a native Clojure data structure."
  [token]
  (into {} token))

(defn unpack
  "Convert a properly formatted Clojure data structure into an AuthToken record."
  [data]
  (map->AuthToken data))

(defn good?
  "Is this token still valid?"
  [token]
  true)                                 ; FIXME: stub; check expiration

(defn generate-random-string
  "Creates a random string by calling `java.util.UUID.randomUUID`."
  []
  (.toString (java.util.UUID/randomUUID)))

(defn generate-token
  "Creates a new Authentication Token."
  ([account-id]
   (unpack {:owner_id account-id :token (generate-random-string)}))
  ([account-id expires]
   (unpack {:owner_id account-id :expires expires :token (generate-random-string)})))
