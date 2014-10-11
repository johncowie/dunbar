(ns dunbar.system
  (:require [com.stuartsierra.component :as component]
            [dunbar.handler :refer [new-web-server new-handler]]
            [dunbar.mongo :refer [new-mongo-db]]
            [dunbar.clock :refer [new-joda-clock]]
            [dunbar.config :refer [load-config]]
            [dunbar.test.test-components :refer [new-test-db]])
  (:gen-class))

(def system (atom {}))

(defn construct-system [config-file]
  (let [config (load-config config-file)]
    (component/system-map
     :db (new-mongo-db config)
     :clock (new-joda-clock)
     :handler (component/using (new-handler) [:db :clock])
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
  (-> (construct-system "config/app.yml")
      (dissoc :webserver)
      (assoc :db (new-test-db))
      start))

(defn lein-ring-handler [request]
  (when-not (get @system :handler)
    (start-lein))
  ((get-in @system [:handler :handle]) request))

; TODO add destroy method
