(ns dunbar.controller
  (:require [ring.util.response :refer [response not-found redirect]]
            [dunbar.view :refer [hello-page login-form-page]]
            [dunbar.routes :as r]
            [dunbar.utils :refer [wrap-handlers]]
            ))

(defn home [_] (response "Hello"))

(defn hello [{{name :name} :params}]
  (response (hello-page "Hello World!" (str "Hello cruel " name))))

(defn hidden [_] (response "I support Cardiff City!!"))

(defn four-o-four [request] (not-found "Nothing was found :-("))

(defn login-form [request] (response (login-form-page "Login")))

(defn login [request]
  (let [username (get-in request [:params :username])]
    (->
     (response (format "You have logged in as user %s" username))
     (assoc-in [:session :username] username))))

(defn logout [request]
  (->
   (redirect (r/path :login))
   (dissoc :session)))

(defn not-logged-in [request] (redirect (r/path :login-form)))



; MAKE controller functions available...
(defn logged-in? [request]
  (get-in request [:session :username]))

(defn wrap-secure [handler]
  (fn [request]
    (if (logged-in? request) (handler request) (not-logged-in request))))

(defn handlers []
  (->
   {:home home
    :hello hello
    :hidden hidden
    :login login
    :logout logout
    :login-form login-form}
   (wrap-handlers [:hidden] wrap-secure)))
