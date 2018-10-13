(ns storage.utils
  "Utilities for storage methods"
  (:gen-class)
  (:require [java-time :as jt]))

(defn sql-now
  "Returns SQL-formatted timestamp for now"
  []
  (jt/format "YYYY-MM-dd HH:mm:ss" (jt/zoned-date-time)))
