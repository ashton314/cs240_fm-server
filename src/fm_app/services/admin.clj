(ns fm-app.services.admin
  "Administrative services"
  (:gen-class)
  (:require (fm-app.models [account :as account]
                           [person  :as person]
                           [auth-token :as token]
                           [faker :as faker])
            (fm-app.storage-protocols [account :as account-proto]
                                      [person :as person-proto]
                                      [auth-token :as token-proto]
                                      [event :as event-proto])))

(defn clear-storage
  "Wipes all records from storage."
  [storage logging]
  (assert (not-any? nil? (vals (select-keys storage [:account :person :auth-token :event])))
          "Missing storage methods in call to fm-app.services.admin/clear-storage")
  (do
    (account-proto/drop-all! (:account storage))
    (person-proto/drop-all! (:person storage))
    (token-proto/drop-all! (:auth-token storage))
    (event-proto/drop-all! (:event storage))))

(defn load-person
  "Wipes all records, then adds a new Person record."
  [person storage logging]
  (do (clear-storage storage)
      #_(let [new-id (person-storage/create storage)]
        (person/pack (conj person {:id new-id}))))
  nil)

#_(defmacro with-new
  "Creates a new instance of a thingy and returns [id obj]"
  [prototype storage packed-obj]
  `(let [store ~storage
         new-id (~(str prototype "/" "create!") store)]
     (~(str prototype "/" "save!") store (conj packed-obj {:id new-id}))))

(defn register
  "Create a new account"
  [account-details storage logging]
  (assert (nil? (account-proto/find-username (:account storage) (:username account-details)))
          "Username already in use")
  (let [new-account (account/unpack account-details)
        new-id      (account-proto/create! (:account storage))
        token       (token/generate-token new-id)]

    ;; TODO: abstract this little dance into a macro
    (let [token-id (token-proto/create! (:auth-token storage))]
      (token-proto/save! (:auth-token storage) (conj token {:id token-id})))


    (let [root-person (person/unpack {:first_name (:first_name account-details)
                                      :last_name (:last_name account-details)
                                      :gender (:gender account-details)
                                      :id (person-proto/create! (:person storage))
                                      :owner_id new-id})

          family (person/populate-ancestry root-person 4 faker/gen-name #(person-proto/create! (:person storage)))
          events (person/make-events (first family) family 1990 #(event-proto/create! (:event storage)) faker/gen-location faker/gen-timestamp-from-year)]

      (account-proto/save! (:account storage) (conj new-account {:id new-id :root_person (:id root-person)}))

      (doall
       (map #(person-proto/save! (:person storage) %) family))

      (doall
       (map #(event-proto/save! (:event storage) %)
            (filter (complement #(and (= (:person_id %) (:id root-person)) (= (:event_type %) :death))) events)))

      {:auth_token token
       :username   (:username new-account)
       :person_id  (:id root-person)})))
