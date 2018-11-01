(ns fm-app.services.people
  "People service"
  (:gen-class)
  (:require [fm-app.models.person :as person]
            [fm-app.models.account :as account]
            [fm-app.models.auth-token :as token]

            [fm-app.storage-protocols.account :as account-proto]
            [fm-app.storage-protocols.person :as person-proto]
            [fm-app.storage-protocols.auth-token :as token-proto]))

(defn get-person
  "Gets a Person record specified by ID"
  [storage logger id]
  nil)

(defn lookup-person
  "Finds and returns a Person record(s) that match a set of attributes"
  [attrs]
  nil)
