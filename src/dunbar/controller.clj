(ns dunbar.controller
  (:require [ring.util.response :refer [status response not-found redirect redirect-after-post content-type]]
            [dunbar.view :as v]
            [dunbar.routes :as r]
            [dunbar.store :as s]
            [dunbar.validation :refer [validate validate-with-translations]]
            [dunbar.logic :refer [error->]]
            [dunbar.clock :refer [now]]
            [dunbar.processor :refer [process-friends process-friend]]
            [dunbar.oauth.twitter :as twitter]
            [clj-http.client :refer [generate-query-string]]
            ))

(defn url-with-query-params [params path]
  (let [query-string (generate-query-string params)]
    (if (empty? query-string)
      path
      (str path "?" query-string))))

(defn user [request]
  (get-in request [:session :user]))

(defn username [request]
  (get-in request [:session :user :name]))

(defn navigation
  ([selected]
      (->>
       [{:text "Friends" :action :friend-list}
        {:text "Add"     :action :add-friend-form}]
       (map (fn [n] (if (= (:action n) selected) (assoc n :selected true) n)))
       (map (fn [n] (-> n (assoc :href (r/path (:action n))) (dissoc :action))))))
  ([] (navigation nil)))

(defn html-response [body]
  (-> (response body) (content-type "text/html")))

(defn absolute-url-from-request [request relative-url]
  (format "%s://%s%s%s"
          (name (request :scheme))
          (:server-name request)
          (if (= (:server-port request) 80) "" (str ":" (:server-port request)))
          relative-url))

(defn home [_] (redirect (r/path :friend-list)))

(defn login-form [request]
  (html-response (v/login-form-page "Login")))

(defn friend-form
  [request]
  (html-response (v/friend-form-page "Add friend" (navigation :add-friend-form) (user request) {} {})))

(defn friend-list [db clock request]
  (let [friends (-> (s/load-friends db (username request)) (process-friends clock))]
    (html-response (v/friend-list-page "My friends" (navigation :friend-list) (username request) friends))))

(defn friend-list-update [db clock request]
  (let [id (get-in request [:params :just-seen])]
    (when-let [friend (s/load-friend db (username request) id)]
      (s/update-friend (assoc friend :last-seen (now clock)) db))
    (redirect (r/path :friend-list))))

(defn friend-details [db clock request]
  (when-let [friend (s/load-friend db (username request) (get-in request [:params :id]))]
    (let [processed-friend (process-friend friend clock)
          title (str (:firstname friend) " " (:lastname friend))]
      (html-response (v/friend-details-page title (navigation) (user request) processed-friend)))))

(defn marshall-to-db [params username clock]
  (->
   (select-keys params [:firstname :lastname :notes :meet-freq])
   (update-in [:meet-freq] #(Integer/parseInt %))
   (assoc :user username)
   (assoc :created-at (now clock))))

(defn add-friend [db clock request]
  (let [{:keys [state success]} (error-> (:params request)
                                         validate-with-translations
                                         #(marshall-to-db % (username request) clock)
                                         #(s/add-friend % db))]
    (if success
      (redirect (r/path :friend-list))
      (html-response (v/friend-form-page "Add friend" (navigation) (username request) (:params request) (:errors state))))))

(defn login [twitter-oauth request]
  (let [callback-url (absolute-url-from-request request (r/path :twitter-callback))
        {:keys [request-token authentication-url] :as m} (twitter/get-request-token twitter-oauth callback-url)]
      (assoc-in (redirect-after-post authentication-url) [:session :request-token] request-token)))

(defn twitter-callback [twitter-oauth request]
  (let [request-token  (get-in request [:session :request-token])
        oauth-verifier (get-in request [:params :oauth_verifier])
        user           (twitter/callback twitter-oauth request-token oauth-verifier)
        redirect-path  (get-in request [:params :uri])]
    (if user
      (-> (redirect (r/path :home))
          (assoc-in [:session :user] (select-keys user [:name :id :screen_name])))
    (redirect (r/path :home)))))

(defn logout [request]
  (->
   (redirect (r/path :login))
   (assoc :session {})))

(defn not-logged-in [request]
  ;(redirect (-> request (select-keys [:uri]) (url-with-query-params (r/path :login-form))))
  (redirect (r/path :login-form)))

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

(defn open-handlers [twitter-oauth]
  {:home home
   :login (partial login twitter-oauth)
   :logout logout
   :login-form login-form
   :twitter-callback (partial twitter-callback twitter-oauth)})

(defn handlers [db clock twitter-oauth]
  (merge
   (secure-handlers db clock)
   (open-handlers twitter-oauth)))
