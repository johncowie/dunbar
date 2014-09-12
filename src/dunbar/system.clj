(ns dunbar.system
  (:require [com.stuartsierra.component :as component]
            [dunbar.handler :refer [new-web-server]]
            [dunbar.mongo :refer [new-mongo-db]])
  (:gen-class)
  )

(defn system []
  (component/system-map
   :db (new-mongo-db "localhost" 27017 "dunbar")
   :webserver (component/using (new-web-server) [:db])))

(defn -main [& args]
  (let [started-system (component/start-system (system))]
    ; TODO add shutdown hook to stop system
    ))
