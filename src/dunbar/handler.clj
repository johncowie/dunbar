(ns dunbar.handler
  (:require [ring.util.response :as rp]
            [dunbar.middleware :refer [wrap-404]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.nested-params :refer [wrap-nested-params]]
            [ring.middleware.params :refer [wrap-params]]
            [dunbar.controller :as c]
            [dunbar.utils :refer [wrap-handlers]]
            [bidi.bidi :refer [make-handler]]
            [dunbar.routes :refer [routes]]))

routes

(def logged-in? false)

(defn home [_] (rp/response (str "Home page")))

(defn wrap-secure [handler]
  (fn [request]
    (if logged-in? (handler request) (rp/redirect "/login"))))

(def handlers
  (->
   {:home home
    :hello c/hello
    :hidden c/hidden
    :login c/login}
   (wrap-handlers [:hidden] wrap-secure)))

(defn look-up-handler [id]
  (or
   (id handlers)
   (constantly nil)))

(defn make-app []
  (->
    (make-handler routes look-up-handler)
    wrap-params
    wrap-nested-params
    (wrap-resource "/public")
    (wrap-404 c/four-o-four)
    ))

(def app (make-app))
