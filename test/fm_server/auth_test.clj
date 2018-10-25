(ns fm-server.auth-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]

            [storage.db.account :as account-store]
            [storage.db.auth-token :as auth-token-store]

            [fm-app.storage-protocols.account :as account-proto]
            [fm-app.storage-protocols.auth-token :as auth-token-proto]

            [fm-app.models.account :as account]
            [fm-app.models.auth-token :as auth-token]))


(deftest check-auth
  (testing "password"
    (let [homsar (account/unpack {:id (rand-int 1000) :username "homsar" :password "tinfoil"
                                  :gender :m})
          tok    (account/authenticate homsar "tinfoil")]
      (is (nil? (account/authenticate homsar "aluminum")) "rejected bad password")
      (is (auth-token/good? tok) "token reported as good (noop right now)")
      (is (= (:account_id tok) (:id homsar)) "IDs match"))))
