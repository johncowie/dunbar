(ns dunbar.system
  (:require [com.stuartsierra.component :as component]
            [dunbar.handler :refer [new-web-server make-app]]
            [dunbar.mongo :refer [new-mongo-db]]
            [dunbar.clock :refer [new-joda-clock]]
            [dunbar.config :refer [load-config]]
            [dunbar.components.stubs :refer [new-test-db]]
            [dunbar.oauth.twitter :refer [new-twitter-oauth new-stub-twitter-oauth]]
            [environ.core :refer [env]])
  (:gen-class))

(def system (atom {}))

(defn load-env-port []
  (when (env :port)
    (Integer/parseInt (env :port))))

(defn construct-system []
  (component/system-map
     :db (new-mongo-db (env :mongo-uri))
     :clock (new-joda-clock)
     :twitter-oauth (new-twitter-oauth (env :twitter-key) (env :twitter-secret))
     :webserver (component/using (new-web-server (load-env-port)) [:db :clock :twitter-oauth])))

(defn start [system-map]
  (do
    (println "Starting system...")
    (swap! system (constantly (component/start-system system-map)))))

(defn stop []
  (do
    (println "Stopping system...")
    (component/stop-system @system)))

(defn -main [& [config-file & args]]
  (.addShutdownHook (Runtime/getRuntime) (Thread. #(stop)))
  (start (construct-system)))

;;; stuff for lein ring server ;;;

(def lein-handler (atom nil))

(defn make-system-app [started-system]
  (->>
   [:config :db :clock :twitter-oauth]
   (map (partial get started-system))
   (apply make-app)))

(defn start-lein []
  (-> (construct-system)
      (dissoc :webserver)
      (assoc :db (new-test-db))
      (assoc :twitter-oauth (new-stub-twitter-oauth {:name "John"}))
      start
      make-system-app))

(defn lein-ring-handler [request] ; TODO fix
  (when-not @lein-handler
    (swap! lein-handler (constantly (start-lein))))
  (@lein-handler request))

; TODO add destroy method
