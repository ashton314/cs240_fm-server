(ns web-server.controllers.resources
  "Handles serving static web resources"
  (:gen-class)
  (:require [ring.util.response :as ring-response]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]))

(defn render
  "Renders a static resource"
  [request params app]
  ((:info (:logger app)) (str "render got request: " request))
  (ring-response/file-response "index.html" {:root "resources/web"}))

(defn render-css
  "Renders a static resource"
  [request params app]
  ((:info (:logger app)) (str "render got request: " request))
  (ring-response/file-response (:filename params) {:root "resources/web/css"}))

(defn render-favicon
  "Renders a static resource"
  [request params app]
  ((:info (:logger app)) (str "render got request: " request))
  (ring-response/file-response "favicon.ico" {:root "resources/web"}))
