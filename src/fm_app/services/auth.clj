(ns fm-app.services.auth
  "Authentication service"
  (:gen-class)
  (:require [fm-app.models.person :as person]
            [fm-app.models.account :as account]
            [fm-app.models.auth-token :as token]

            [fm-app.storage-protocols.account :as account-proto]
            [fm-app.storage-protocols.person :as person-proto]
            [fm-app.storage-protocols.auth-token :as token-proto]))

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

(defn good-token-for-account?
  "Given the token string and an account id, check whether or not that token matches the account id."
  [storage token-string account-id]
  (token-proto/fetch storage token-string))

(defn change-password
  "Changes Account password."
  [storage account passwd]
  (account-proto/save! storage (account/pack (account/set-password account passwd))))
