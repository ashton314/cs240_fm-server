(ns fm-app.models.person
  "A person in a family tree.

  ### Person

  Fields:

   - `id` unique identifier
   - `first-name`
   - `last-name`
   - `gender` either `:m` or `:f`
   - `father` ID of father (another Person object)
   - `mother` ditto
   - `spouse` ditto
   - `owner-id` ID of Account that this belongs to
  "
  (:gen-class))


(defrecord Person [id first-name last-name gender father mother spouse owner-id])

(defn owned-by?
  "Returns whether or not this Person is owned by a given Account."
  [person account]
  (= (:owner-id person) (:id account)))

(defn pack
  "Change an person into a native Clojure data structure."
  [person]
  (into {} person))

(defn unpack
  "Convert a properly formatted Clojure data structure into an Person record."
  [data]
  (map->Person data))

(defn marry
  "Sets to Person's spouse records equal to each other. Returns a vector
  in the same order the people were given."
  [man woman]
  [(conj man   {:spouse (:id woman)})
   (conj woman {:spouse (:id man)})])

(defn default-gen-name
  "Default name generation function. Doesn't produce very interesting results."
  [gender placement]
  (rand-nth (if (= placement :first)
              (if (= gender :m)
                ["Arthur" "Bob" "Charley"]
                ["Alice" "Brooke" "Cassidy"])
              ["Adams" "Brady" "Jackson"])))

(defn populate-ancestry
  "Generate a family tree for some levels with random information for
  names and such. Returns a collection of newly-created Person
  records. Also takes a name generation function, as well as an ID
  generation function."
  [person levels gen-name gen-id]
   (if (= levels 0)
     person
     (let [woman (unpack (conj (dissoc person :id)
                               {:first-name (gen-name :f :first) :last-name (gen-name :f :last) :gender :f :id (gen-id)}))
           man (unpack (conj (dissoc person :id)
                             {:first-name (gen-name :m :first) :last-name (gen-name :m :last) :gender :m :id (gen-id)}))]
       (let [[dad mom] (marry man woman)]
         (flatten (conj (map #(populate-ancestry % (- levels 1) gen-name gen-id)
                             [dad mom])
                        (conj person {:father (:id dad) :mother (:id mom)})))))))

      