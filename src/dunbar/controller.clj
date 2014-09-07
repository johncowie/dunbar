(ns dunbar.controller
  (:require [ring.util.response :refer [response not-found redirect]]
            [dunbar.view :as v]
            [dunbar.routes :as r]
            [dunbar.utils :refer [wrap-handlers]]
            ))

(defn home [_] (response "Hello"))

(defn hello [{{name :name} :params}]
  (response (v/hello-page "Hello World!" (str "Hello cruel " name))))

(defn four-o-four [request] (not-found "Nothing was found :-("))

(defn login-form [request] (response (v/login-form-page "Login")))

(defn friend-form [request] (response (v/friend-form-page "Add friend")))

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
    :login login
    :logout logout
    :login-form login-form
    :add-friend-form friend-form}
   (wrap-handlers [:hidden] wrap-secure)))
