(defproject fm-server "0.1.0"
  :description "Family Map Server"
  :url "http://example.com/FIXME"
  ;; :license {:name "Eclipse Public License"
  ;;           :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.0"]
                 [clojure.java-time "0.3.2"]
                 [mount "0.1.13"]

                 [ring "1.7.0"]
                 [ring/ring-mock "0.3.2"]
                 [clout "2.2.1"]

                 [org.clojure/data.json "0.2.6"]     ; looks like all "org.clojure" libs come from https://clojure.github.io/
                 [org.xerial/sqlite-jdbc "3.25.2"]

                 [org.clojure/tools.logging "0.4.1"] ; https://github.com/clojure/tools.logging
                 [org.clojure/tools.trace "0.7.10"]  ; https://github.com/clojure/tools.trace
                 ]
  :main ^:skip-aot cl-interface
  :plugins [[lein-codox "0.10.5"]]
  :codox {:metadata {:doc/format :markdown}}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
