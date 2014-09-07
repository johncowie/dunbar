(defproject dunbar "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.1"]
                 [bidi "1.10.5-SNAPSHOT"]
                 [prismatic/schema "0.2.6"]
                 [traversy "0.2.0"]
                 [midje "1.6.3"]
                 [com.novemberain/monger "2.0.0"]
                 [enlive "1.1.5"]
                 [com.stuartsierra/component "0.2.1"]
                 ]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler dunbar.handler/app}
  :main dunbar.system
  :user {:plugins [[lein-midje "3.1.1"]]}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}})
