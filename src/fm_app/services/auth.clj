(ns fm-app.services.auth
  "Authentication service"
  (:gen-class)
  (:require [fm-app.models.person :as person]
            [fm-app.models.account :as account]))

(defn authenticate
  "Takes a username and a password. Returns an AuthToken if password is good."
  [storage username passwd]
  ;; this should do something like
  ;; (-> username
  ;;     (storage find-username)
  ;;     account/unpack
  ;;     (account/correct-password? passwd))
  nil)

(defn revoke-token
  "Revokes an AuthToken."
  [storage token]
  nil)

(defn change-password
  "Changes Account password."
  [storage account passwd]
  ;; TODO: store this
  (account/set-password account passwd))
