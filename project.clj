(defproject brunneby "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"] 
                 [lib-noir "0.4.6"]
                 [enlive "1.1.1"]
                 [org.clojure/java.jdbc "0.3.0-alpha1"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 [korma "0.3.0-RC5"]]


  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler brunneby.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
