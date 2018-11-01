(ns fm-app.services.auth
  "Authentication service"
  (:gen-class)
  (:require [fm-app.models.person :as person]
            [fm-app.models.account :as account]
            [fm-app.models.auth-token :as token]

            [fm-app.storage-protocols.account :as account-proto]
            [fm-app.storage-protocols.person :as person-proto]
            [fm-app.storage-protocols.auth-token :as token-proto]))

(defn find-account
  "Returns an Account record given a username, or nil."
  [account-storage logger username]
  (if-let [account (account-proto/find-username account-storage username)]
    (account/unpack account)))

(defn find-token
  [storage logger token]
  (if-let [token (token-proto/fetch storage token)]
    (token/unpack token)))

(defn find-account-by-token
  "Retrieve an account by token."
  [storage logger token]
  (if-let [token (find-token (:auth-token storage) logger token)]
    (if-let [account (account-proto/fetch (:account storage) (:owner_id token))]
      (account/unpack account))))

(defn authenticate
  "Takes a username and a password. Returns an AuthToken if password is good."
  [storage logger username passwd]
  (if-let [account (find-account (:account storage) logger username)]
    (if-let [token (account/authenticate account passwd)]
      (do (token-proto/save! (:auth-token storage) (conj token
                                                         {:id (token-proto/create!
                                                               (:auth-token storage))}))
                                                         
          token))))

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
