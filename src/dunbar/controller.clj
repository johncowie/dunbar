(ns dunbar.controller
  (:require [ring.util.response :refer [response not-found redirect content-type]]
            [dunbar.view :as v]
            [dunbar.routes :as r]
            [dunbar.store :as s]
            [dunbar.validation :refer [validate validate-with-translations]]
            [dunbar.logic :refer [error->]]))

(defn username [request]
  (get-in request [:session :username]))

(defn navigation []
  [{:text "Friends" :href (r/path :friend-list)}
   {:text "Add" :href (r/path :add-friend-form)}])

(defn html-response [body]
  (-> (response body) (content-type "text/html")))

(defn home [_] (redirect (r/path :friend-list)))

(defn four-o-four [request] (not-found "Nothing was found :-("))

(defn login-form [request]
  (html-response (v/login-form-page "Login")))

(defn friend-form
  [request]
  (html-response (v/friend-form-page "Add friend" (navigation) {} {})))

(defn friend-list [db request]
  (let [friends (s/load-friends db (username request))]
    (html-response (v/friend-list-page "My friends" (navigation) friends))))

(defn marshall-to-db [params]
  (->
   (select-keys params [:firstname :lastname :notes :meet-freq])
   (update-in [:meet-freq] #(Integer/parseInt %))))

(defn add-friend [db request]
  (let [{:keys [state success]} (error-> (:params request)
                                         validate-with-translations
                                         marshall-to-db
                                         #(s/add-friend % db (username request)))]
    (if success
      (redirect (r/path :friend-list))
      (html-response (v/friend-form-page "Add friend" (navigation) (:params request) (:errors state))))))

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
  (username request))

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
