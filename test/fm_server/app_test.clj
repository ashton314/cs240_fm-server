(ns fm-server.app-test
  (:require [clojure.test :refer :all]
            [storage.protocols.account :as account-proto]
            [storage.mem.account :as account-store]
            [fm-app.models.account :as account])
  (:gen-class))

(deftest smoke
  (testing "smoke test"
    (is (= 1 1))))
