(ns dunbar.controller
  (:require [ring.util.response :refer [status response not-found redirect content-type]]
            [dunbar.view :as v]
            [dunbar.routes :as r]
            [dunbar.store :as s]
            [dunbar.validation :refer [validate validate-with-translations]]
            [dunbar.logic :refer [error->]]
            [dunbar.clock :refer [now]]
            [dunbar.processor :refer [process-friends]]))

(defn username [request]
  (get-in request [:session :username]))

(defn navigation []
  [{:text "Friends" :href (r/path :friend-list)}
   {:text "Add" :href (r/path :add-friend-form)}])

(defn html-response [body]
  (-> (response body) (content-type "text/html")))

(defn home [_] (redirect (r/path :friend-list)))

(defn login-form [request]
  (html-response (v/login-form-page "Login")))

(defn friend-form
  [request]
  (html-response (v/friend-form-page "Add friend" (navigation) {} {})))

(defn friend-list [db clock request]
  (let [friends (-> (s/load-friends db (username request)) (process-friends clock))]
    (html-response (v/friend-list-page "My friends" (navigation) friends))))

(defn friend-list-update [db clock request]
  (let [id (get-in request [:params :just-seen])]
    (when-let [friend (s/load-friend db (username request) id)]
      (s/update-friend (assoc friend :last-seen (now clock)) db))
    (redirect (r/path :friend-list))))

(defn friend-details [db request]
  (let [friend (s/load-friend db (username request) (get-in request [:params :id]))]
    (html-response (v/friend-details-page (str (:firstname friend) " " (:lastname friend)) (navigation) friend))))

(defn generate-id [firstname lastname]
  (str (clojure.string/lower-case firstname)
       "-"
       (clojure.string/lower-case lastname)))

(defn marshall-to-db [params username clock]
  (->
   (select-keys params [:firstname :lastname :notes :meet-freq])
   (update-in [:meet-freq] #(Integer/parseInt %))
   (assoc :id (generate-id (:firstname params) (:lastname params)))
   (assoc :user username)
   (assoc :created-at (now clock))))

(defn add-friend [db clock request]
  (let [{:keys [state success]} (error-> (:params request)
                                         validate-with-translations
                                         #(marshall-to-db % (username request) clock)
                                         #(s/add-friend % db))]
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

(defn wrap-secure [handler]
  (fn [request]
    (if (username request) (handler request) (not-logged-in request))))

(defn error [request]
  (-> (v/server-error-page "UH OH." (navigation) (username request))
      html-response
      (status 500)))

(defn four-o-four [request]
  (->
   (v/not-found-page "Nothing to see here.." (navigation) (username request))
   html-response
   (status 404)))

(defn map-over-vals [m & fs]
  (into {} (map (fn [[k v]] [k (reduce #(%2 %1) v fs)]) m)))

(defn secure-handlers [db clock]
  (->
   {:add-friend-form friend-form
    :add-friend (partial add-friend db clock)
    :friend-list (partial friend-list db clock)
    :friend-list-update (partial friend-list-update db clock)
    :friend-details (partial friend-details db)}
   (map-over-vals wrap-secure)))

(defn open-handlers []
  {:home home
   :login login
   :logout logout
   :login-form login-form})

(defn handlers [db clock]
  (merge (secure-handlers db clock) (open-handlers)))
