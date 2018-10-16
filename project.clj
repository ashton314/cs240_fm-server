(defproject fm-server "0.1.0"
  :description "Family Map Server"
  :url "http://example.com/FIXME"
  ;; :license {:name "Eclipse Public License"
  ;;           :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.0"]
                 [clojure.java-time "0.3.2"]
                 [mount "0.1.13"]
                 [org.xerial/sqlite-jdbc "3.25.2"]]
  :main ^:skip-aot web-server
  :plugins [[lein-codox "0.10.5"]]
  :codox {:metadata {:doc/format :markdown}}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
