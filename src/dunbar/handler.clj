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

(defn look-up-handler [db]
  (fn [id]
    (or
     (id (c/handlers db))
     (constantly nil))))

(defn make-app [db]
  (->
   (make-handler routes (look-up-handler db))
    wrap-session
    wrap-keyword-params
    wrap-nested-params
    wrap-params
    wrap-multipart-params
    wrap-content-type
    (wrap-resource "/public")
    (wrap-404 c/four-o-four)
    (wrap-error-handling c/error)
    ))

(defrecord Handler [db]
  component/Lifecycle
  (start [this]
    (assoc this :handle (make-app db)))
  (stop [this]
    (dissoc this :handle)))

(defn new-handler
  ([] (new-handler {}))
  ([dependencies] (map->Handler dependencies)))

(defrecord WebServer [port handler]
  component/Lifecycle
  (start [this]
    (assoc this :server (run-jetty (:handle handler) {:port port})))
  (stop [this]
    (doto (:server this) .join .stop)
    (dissoc this :server)))

(defn new-web-server [{{port :port} :app}]
  (map->WebServer {:port port}))
