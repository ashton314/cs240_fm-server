(ns fm-app.services.auth
  "Authentication service"
  (:gen-class)
  (:require [fm-app.models.person :as person]
            [fm-app.models.account :as account]
            [fm-app.storage-protocols.account :as account-proto]
            [fm-app.storage-protocols.person :as person-proto]))

(defn authenticate
  "Takes a username and a password. Returns an AuthToken if password is good."
  [storage username passwd]
  (if-let [account (account-proto/find-username storage username)]
    (-> account
        account/unpack
        (account/authenticate passwd))))

(defn revoke-token
  "Revokes an AuthToken."
  [storage token]
  nil)

(defn change-password
  "Changes Account password."
  [storage account passwd]
  (account-proto/save! (account/pack (account/set-password account passwd))))
