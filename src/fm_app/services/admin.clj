(ns fm-app.services.admin
  "Administrative services"
  (:gen-class)
  (:require (fm-app.models [account :as account]
                           [person  :as person]
                           [auth-token :as token])
            (fm-app.storage-protocols [account :as account-proto]
                                      [person :as person-proto]
                                      [auth-token :as token-proto])))
            

(defn clear-storage
  "Wipes all records from storage."
  [storage-map]
  nil)

(defn load-person
  "Wipes all records, then adds a new Person record."
  [storage person]
  (do (clear-storage storage)
      #_(let [new-id (person-storage/create storage)]
        (person/pack (conj person {:id new-id}))))
  nil)

(defmacro with-new
  "Creates a new instance of a thingy and returns [id obj]"
  [prototype storage packed-obj]
  `(let [store ~storage
         new-id (~(str prototype "/" "create!") store)]
     (~(str prototype "/" "save!") store (conj packed-obj {:id new-id}))))
  

(defn register
  "Create a new account"
  [account-details storage logging]
  (assert (= #{:username :password :first_name :last_name :email :gender}
             (into #{} (keys account-details)))
          (str "field mismatch, should be " #{:username :password :first_name :last_name :email :gender}))
  ;; TODO: check to make sure username not already taken
  ;; TODO: check to make sure no datamembers are null
  (let [new-account (account/unpack account-details)
        new-id      (account-proto/create! (:account storage))
        token       (token/generate-token new-id)]

    ;; TODO: abstract this little dance into a macro
    (let [token-id (token-proto/create! (:auth-token storage))]
      (token-proto/save! (:auth-token storage) (conj token {:id token-id})))

    (account-proto/save! (:account storage) (conj new-account {:id new-id}))
    
    {:auth_token token
     :username  (:username new-account)
     :person_id  nil}))
    
