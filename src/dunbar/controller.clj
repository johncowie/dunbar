(ns dunbar.controller
  (:require [ring.util.response :refer [status response not-found redirect redirect-after-post content-type]]
            [dunbar.view :as v]
            [dunbar.routes :as r]
            [dunbar.store :as s]
            [dunbar.validation :refer [validate validate-with-translations]]
            [dunbar.logic :refer [error->]]
            [dunbar.clock :refer [now]]
            [dunbar.processor :refer [process-friends process-friend]]
            [dunbar.oauth.twitter :as twitter]))

(defn username [request]
  (get-in request [:session :user :name]))

(defn navigation []
  [{:text "Friends" :href (r/path :friend-list)}
   {:text "Add" :href (r/path :add-friend-form)}])

(defn html-response [body]
  (-> (response body) (content-type "text/html")))

(defn absolute-url [config relative-url]
  (str (:external-url config) relative-url))

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

(defn friend-details [db clock request]
  (let [friend (s/load-friend db (username request) (get-in request [:params :id]))
        processed-friend (process-friend friend clock)]
    (html-response (v/friend-details-page (str (:firstname friend) " " (:lastname friend)) (navigation) processed-friend))))

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

(defn login [config twitter-oauth request]
  (let [{:keys [request-token authentication-url] :as m} (twitter/get-request-token twitter-oauth (absolute-url config "/oauth/twitter"))]
      (assoc-in (redirect-after-post authentication-url) [:session :request-token] request-token)))

(defn twitter-callback [twitter-oauth request]
  (let [request-token  (get-in request [:session :request-token])
        oauth-verifier (get-in request [:params :oauth_verifier])
        user           (twitter/callback twitter-oauth request-token oauth-verifier)]
    (-> (redirect (r/path :home))
        (assoc-in [:session :user] (select-keys user [:name :id :screen_name])))))

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
    :friend-details (partial friend-details db clock)}
   (map-over-vals wrap-secure)))

(defn open-handlers [config twitter-oauth]
  {:home home
   :login (partial login config twitter-oauth)
   :logout logout
   :login-form login-form
   :twitter-callback (partial twitter-callback twitter-oauth)})

(defn handlers [db clock twitter-oauth config]
  (merge
   (secure-handlers db clock)
   (open-handlers config twitter-oauth)))
