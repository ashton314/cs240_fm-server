(ns storage.utils
  "Utilities for storage methods"
  (:gen-class)
  (:require [java-time :as jt]
            [clojure.java.jdbc :as jdbc]
            [mount.core :as mount]))

(defn sql-now
  "Returns SQL-formatted timestamp for now"
  []
  (jt/format "YYYY-MM-dd HH:mm:ss" (jt/zoned-date-time)))

(declare mem-socket)

(defn on-start []
  (let [spec {:connection-uri "jdbc:sqlite::memory:"}
        conn (jdbc/get-connection spec)]
    (assoc spec :connection conn)))

(defn on-stop []
  (-> mem-socket :connection .close)
  nil)

(mount/defstate
  ^{:on-reload :noop}
  mem-socket
  :start (on-start)
  :stop (on-stop))

;; (defn start-conn
;;   (mount/start #'mem-socket))
