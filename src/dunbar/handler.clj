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
            [scenic.routes :refer [scenic-handler]]
            [dunbar.routes :refer [routes]]
            [com.stuartsierra.component :as component]))

(defn make-app [db clock twitter-oauth]
  (->
   (scenic-handler routes (c/handlers db clock twitter-oauth) c/four-o-four)
    wrap-session
    wrap-keyword-params
    wrap-nested-params
    wrap-params
    wrap-multipart-params
    wrap-content-type
    (wrap-resource "/public")
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
