(ns storage.mem.account
  "In-memory backend for storing Accounts

  All these methods return *packed* objects. They should be passed to
  the Account's `unpack` function to get a vitalized record."
  (:gen-class))

(defn save-account!
  "Takes packed Account and saves it."
  [account]
  nil)

(defn new-account!
  "Reserves a slot in the database and returns an ID for a new Account record."
  []
  nil)

(defn find-username
  "Looks for an Account with a given username and returns it if it exists."
  [username]
  nil)
