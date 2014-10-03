(ns dunbar.system
  (:require [com.stuartsierra.component :as component]
            [dunbar.handler :refer [new-web-server new-handler]]
            [dunbar.mongo :refer [new-mongo-db]]
            [dunbar.config :refer [load-config]])
  (:gen-class)
  )

(def system (atom {}))

(defn construct-system [config-file]
  (let [config (load-config config-file)]
    (component/system-map
     :db (new-mongo-db config)
     :handler (component/using (new-handler) [:db])
     :webserver (component/using (new-web-server config) [:handler]))))

(defn start [system-map]
  (do
    (println "Starting system...")
    (swap! system (constantly (component/start-system system-map)))))

(defn stop []
  (do
    (println "Stopping system...")
    (component/stop-system @system)))

(defn -main [config-file & args]
  (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop)))
  (start (construct-system config-file)))

;;; stuff for lein ring server ;;;

(defn start-lein []
  (start (dissoc (construct-system "config/app.yml") :webserver)))

(defn lein-ring-handler [request]
  (when-not (get @system :handler)
    (start-lein))
  ((get-in @system [:handler :handle]) request))
