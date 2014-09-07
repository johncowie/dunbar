(ns dunbar.system
  (:require [com.stuartsierra.component :as component]
            [dunbar.handler :refer [new-web-server]])
  (:gen-class)
  )

(defn system []
  (component/system-map
   :webserver (new-web-server)))

(defn -main [& args]
  (component/start-system (system)))
