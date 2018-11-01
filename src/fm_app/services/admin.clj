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

(defn load-records
  "Loads records into storage"
  [storage logging users persons events]
  (let [account-idx (into {} (map #(vector (:username %) (account-proto/create! (:account storage))) users))
        person-idx  (into {} (map #(vector (:id %) (person-proto/create! (:person storage))) persons))
        event-idx   (into {} (map #(vector (:id %) (event-proto/create! (:event storage))) events))]
    (doall
     (map #(account-proto/save! (:account storage) (conj % {:id (account-idx (:username %))
                                                            :root_person (person-idx (:root_person %))}))
          users))
    (doall
     (map #(person-proto/save! (:person storage) (conj % {:id (person-idx (:id %))
                                                          :spouse (and (:spouse %) (person-idx (:spouse %)))
                                                          :mother (and (:mother %) (person-idx (:mother %)))
                                                          :father (and (:father %) (person-idx (:father %)))
                                                          :owner_id (account-idx (:owner_id %))}))
          persons))
    (doall
     (map #(event-proto/save! (:event storage) (dissoc (conj % {:id (event-idx (:id %))
                                                                :timestamp (str (:year %) "-01-01")
                                                                :person_id (person-idx (:person_id %))
                                                                :owner_id (account-idx (:owner_id %))}) :year))
          events))
    {:people persons
     :events events
     :accounts users
     :people_map person-idx
     :event_map event-idx}))

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

(defn clear-account
  "Wipes all records associated with an account."
  [account storage logging]
  (person-proto/drop-by-owner! (:person storage) (:id account))
  (token-proto/drop-by-owner! (:auth-token storage) (:id account))
  (event-proto/drop-by-owner! (:event storage) (:id account)))

(defn fill-account
  [account generations storage logging]
  (let [root-person (person/unpack {:first_name (:first_name account)
                                    :last_name (:last_name account)
                                    :gender (:gender account)
                                    :id (person-proto/create! (:person storage))
                                    :owner_id (:id account)})

        family (person/populate-ancestry root-person generations faker/gen-name #(person-proto/create! (:person storage)))
        events (->> (person/make-events (first family) family 1990 #(event-proto/create! (:event storage)) faker/gen-location faker/gen-timestamp-from-year)
                    (filter (complement #(and (= (:person_id %) (:id root-person)) (= (:event_type %) :death)))))] ; all the events *EXCEPT* the death of the current root person


    (account-proto/save! (:account storage) (conj account {:root_person (:id root-person)}))

    (doall
     (map #(person-proto/save! (:person storage) %) family))

    (doall
     (map #(event-proto/save! (:event storage) %)
          events))

    {:people family
     :events events}))

(defn register
  "Create a new account"
  [account storage logging]
  (assert (nil? (account-proto/find-username (:account storage) (:username account)))
          "Username already in use")
  (let [new-account (account/unpack account)
        new-id      (account-proto/create! (:account storage))
        token       (token/generate-token new-id)]

    (let [token-id (token-proto/create! (:auth-token storage))]
      (token-proto/save! (:auth-token storage) (conj token {:id token-id})))


    (let [root-person (person/unpack {:first_name (:first_name account)
                                      :last_name (:last_name account)
                                      :gender (:gender account)
                                      :id (person-proto/create! (:person storage))
                                      :owner_id new-id})

          family (person/populate-ancestry root-person 4 faker/gen-name #(person-proto/create! (:person storage)))
          events (person/make-events (first family) family 1990 #(event-proto/create! (:event storage)) faker/gen-location faker/gen-timestamp-from-year)]

      (account-proto/save! (:account storage) (conj new-account {:id new-id :root_person (:id root-person)}))

      (doall
       (map #(person-proto/save! (:person storage) %) family))

      (doall
       (map #(event-proto/save! (:event storage) %) ; save all the events *EXCEPT* the death of the current root person
            (filter (complement #(and (= (:person_id %) (:id root-person)) (= (:event_type %) :death))) events)))

      {:auth_token token
       :username   (:username new-account)
       :person_id  (:id root-person)})))
