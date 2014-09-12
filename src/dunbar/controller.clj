(ns dunbar.controller
  (:require [ring.util.response :refer [response not-found redirect content-type]]
            [dunbar.view :as v]
            [dunbar.routes :as r]
            [dunbar.utils :refer [wrap-handlers]]
            [dunbar.store :as s]
            ))


(defn html-response [body]
  (-> (response body) (content-type "text/html")))

(defn home [_] (html-response "Hello"))

(defn hello [{{name :name} :params}]
  (html-response (v/hello-page "Hello World!" (str "Hello cruel " name))))

(defn four-o-four [request] (not-found "Nothing was found :-("))

(defn login-form [request] (html-response (v/login-form-page "Login")))

(defn friend-form [request] (html-response (v/friend-form-page "Add friend")))

(defn friend-list [db]
  (fn [request]
    (let [username (get-in request [:session :username])
          friends (s/load-friends db username)]
      (html-response (v/friend-list-page "My friends" friends)))))

(defn add-friend [db]
  (fn  [request]
    (let [username (get-in request [:session :username])]
      (-> (select-keys (:params request) [:firstname :lastname])
          (s/add-friend db username)))
    (redirect (r/path :friend-list))))

(defn login [request]
  (let [username (get-in request [:params :username])]
    (->
     (redirect (r/path :friend-list))
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

(defn apply-fn [v f] (f v))

(defn map-over-vals [m & fs]
  (into {} (map (fn [[k v]] [k (reduce apply-fn v fs)]) m)))

(defn secure-handlers [db]
  (->
   {:add-friend-form friend-form
    :add-friend (add-friend db)
    :friend-list (friend-list db)}
   (map-over-vals wrap-secure)))

(defn open-handlers []
  {:home home
   :hello hello
   :login login
   :logout logout
   :login-form login-form})

(defn handlers [db]
  (merge (secure-handlers db) (open-handlers)))
