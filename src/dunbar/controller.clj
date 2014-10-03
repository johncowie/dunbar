(ns dunbar.controller
  (:require [ring.util.response :refer [response not-found redirect content-type]]
            [dunbar.view :as v]
            [dunbar.routes :as r]
            [dunbar.store :as s]))

(defn navigation []
  [{:text "Friends" :href (r/path :friend-list)}
   {:text "Add" :href (r/path :add-friend-form)}])

(defn html-response [body]
  (-> (response body) (content-type "text/html")))

(defn home [_] (redirect (r/path :friend-list)))

(defn four-o-four [request] (not-found "Nothing was found :-("))

(defn login-form [request] (html-response (v/login-form-page "Login" (navigation))))

(defn friend-form [request] (html-response (v/friend-form-page "Add friend" (navigation))))

(defn friend-list [db request]
  (let [username (get-in request [:session :username])
        friends (s/load-friends db username)]
    (html-response (v/friend-list-page "My friends" (navigation) friends))))

(defn add-friend [db request]
  (let [username (get-in request [:session :username])]
    (-> (select-keys (:params request) [:firstname :lastname])
        (s/add-friend db username)))
  (redirect (r/path :friend-list)))

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
    :add-friend (partial add-friend db)
    :friend-list (partial friend-list db)}
   (map-over-vals wrap-secure)))

(defn open-handlers []
  {:home home
   :login login
   :logout logout
   :login-form login-form})

(defn handlers [db]
  (merge (secure-handlers db) (open-handlers)))
