(ns dunbar.view
  (:require [net.cgrand.enlive-html :as html]
            [dunbar.routes :as r]
            [clj-time.coerce :as time-coerce]
            [clj-time.format :as time-format]))

(def date-formatter (time-format/formatter "dd MMM YYYY"))

(defn show-date [millis]
  (clojure.string/upper-case
     (time-format/unparse date-formatter (time-coerce/from-long millis))))

(defn css-select [s]
  [(keyword s)])

(def style-guide "public/templates/index.html")

(html/defsnippet navigation-login-snippet style-guide [:#navigation-login]
  [logged-in?]
  [:ul [:li html/first-of-type] :a] (html/content (if logged-in? "Logout" "Login"))
  [:ul [:li html/first-of-type] :a] (html/set-attr :href (r/path (if logged-in? :logout :login)))
  )

(html/defsnippet navigation-snippet style-guide [:#navigation]
  [nav-links]
  [:ul [:li (html/but html/first-of-type)]] nil ; remove all but first dummy link
  [:ul [:li html/first-of-type]]
  (html/clone-for [{href :href text :text} nav-links]
                  [:li :a] (html/content text)
                  [:li :a] (html/set-attr :href href)))

(html/deftemplate index-page-template style-guide
  [title nav-links logged-in? content-snippet]
  [:title] (html/content title)
  [:#navigation] (when logged-in? (html/substitute (navigation-snippet nav-links)))
  [:#navigation-login] (html/substitute (navigation-login-snippet logged-in?))
  [:#content] (html/content content-snippet))

(html/defsnippet login-form-snippet style-guide [:#login]
  [])

(html/defsnippet validation-errors-snippet style-guide [:#friend-form :.validation-errors]
  [errors]
  [:ul [:li (html/but html/first-of-type)]] nil
  [:ul [:li html/first-of-type]]
  (html/clone-for [text (vals errors)]
                  [:li] (html/content (str text))))

(def meet-freq-dropdown-values
  [{:value "1" :text "day"}
   {:value "7" :text "week"}
   {:value "28" :text "month"}
   {:value "365" :text "year"}])  ; TODO move this to models

(html/defsnippet friend-form-snippet style-guide [:#friend-form]
  [posted-data errors]
  [:.validation-errors] (when-not (empty? errors)
                          (html/substitute (validation-errors-snippet errors)))
  [[:input (html/attr= :name "firstname")]] (html/set-attr :value (:firstname posted-data))
  [[:input (html/attr= :name "lastname")]] (html/set-attr :value (:lastname posted-data))
  [[:textarea (html/attr= :name "notes")]] (html/set-attr :value (:notes posted-data))
  [[:select (html/attr= :name "meet-freq")] [:option (html/but html/first-of-type)]] nil
  [[:select (html/attr= :name "meet-freq")] [:option html/first-of-type]]
  (html/clone-for [{value :value text :text} meet-freq-dropdown-values]
                  [:option] (html/content text)
                  [:option] (html/set-attr :value value))
  [[:select (html/attr= :name "meet-freq")] [:option (html/attr= :value (:meet-freq posted-data))]]
  (html/set-attr :selected "selected"))

(html/defsnippet friend-list-snippet style-guide [:#friend-list]
  [friends]
  [:table :tr.friend-row]
  (html/clone-for [{:keys [firstname lastname notes meet-freq id]} friends]
                  [:tr.friend-row :.friend-name :a]
                    (html/content (str firstname " " lastname))
                  [:tr.friend-row :.friend-name :a]
                    (html/set-attr :href (r/path :friend-details :id id))
                  [:tr.friend-row :.friend-meet-freq]
                    (html/content (str meet-freq))
                  [:tr.friend-row :.friend-just-seen [:input (html/attr= :type "hidden")]]
                    (html/set-attr :value id)
                  [:tr.friend-row :.friend-just-seen [:input (html/attr= :type "hidden")]]
                    (html/set-attr :name "just-seen")
                  [:tr.friend-row :.friend-just-seen :form]
                    (html/set-attr :action (r/path :friend-list-update))
                  [:tr.friend-row :.friend-just-seen :form]
                    (html/set-attr :method "POST")))

(html/defsnippet friend-details-snippet style-guide [:#friend-details]
  [{:keys [firstname lastname meet-freq notes last-seen]}]
  [:#friend-details-name] (html/content (str firstname " " lastname))
  [:#friend-details-meet-freq-firstname] (html/content firstname)
  [:#friend-details-meet-freq] (html/content (str meet-freq))
  [:#friend-details-notes] (html/content notes)
  [:#friend-details-last-seen] (html/content (show-date last-seen)))

(html/defsnippet not-found-snippet style-guide [:#not-found] [])
(html/defsnippet server-error-snippet style-guide [:#server-error] [])

(defn page
  [title nav-links logged-in? content]
  (reduce str (index-page-template title nav-links logged-in? content)))

(defn not-found-page [title nav username]
  (page title nav username (not-found-snippet)))

(defn server-error-page [title nav username]
  (page title nav username (server-error-snippet)))

(defn login-form-page [title]
  (page title [] false (login-form-snippet)))

(defn friend-form-page [title nav posted-data errors]
  (page title nav true (friend-form-snippet posted-data errors)))

(defn friend-list-page [title nav friends]
  (page title nav true (friend-list-snippet friends)))

(defn friend-details-page [title nav friend]
  (page title nav true (friend-details-snippet friend)))
