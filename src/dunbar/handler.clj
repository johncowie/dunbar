(ns dunbar.handler
  (:require [ring.util.response :as rp]
            [dunbar.middleware :refer [wrap-404 wrap-error-handling]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.adapter.jetty :refer [run-jetty]]
            [dunbar.controller :as c]
            [bidi.bidi :refer [make-handler]]
            [dunbar.routes :refer [routes]]
            [com.stuartsierra.component :as component]))

(defn look-up-handler [db clock twitter-oauth]
  (fn [id]
    (or
     (id (c/handlers db clock twitter-oauth))
     (do (println "No handler found for id: " id)
         (constantly nil)))))

(defn make-app [db clock twitter-oauth]
  (->
   (make-handler routes (look-up-handler db clock twitter-oauth))
    wrap-session
    wrap-keyword-params
    wrap-nested-params
    wrap-params
    wrap-multipart-params
    wrap-content-type
    (wrap-resource "/public")
    (wrap-404 c/four-o-four)
    (wrap-error-handling c/error)))

(defrecord WebServer [port db clock twitter-oauth]
  component/Lifecycle
  (start [this]
    (assoc this :server (run-jetty (make-app db clock twitter-oauth) {:port port})))
  (stop [this]
    (doto (:server this) .join .stop)
    (dissoc this :server)))

(defn new-web-server [port]
  (map->WebServer {:port port}))
