(ns dunbar.view
  (:require [net.cgrand.enlive-html :as html]
            [dunbar.routes :as r]
            [dunbar.static-data :as data]
            [clj-time.coerce :as time-coerce]
            [clj-time.format :as time-format]))

(def date-formatter (time-format/formatter "dd MMM YYYY"))

(defn show-date [millis]
  (if millis
    (-> date-formatter
       (time-format/unparse (time-coerce/from-long millis))
        clojure.string/upper-case)
    "-"))

(defn css-select [s]
  [(keyword s)])

(def style-guide "public/templates/index.html")
(def bootstrap "public/templates/bootstrap.html")

(html/defsnippet navigation-login-snippet bootstrap [:#navigation-login]
  [logged-in?]
  [[:li html/first-of-type] :a] (html/content (if logged-in? "Logout" "Login"))
  [[:li html/first-of-type] :a] (html/set-attr :href (r/path (if logged-in? :logout :login))))

(html/defsnippet navigation-snippet bootstrap [:#navigation]
  [nav-links]
  [[:li (html/but html/first-of-type)]] nil ; remove all but first dummy link
  [[:li html/first-of-type]]
  (html/clone-for [{href :href text :text} nav-links]
                  [:li] (html/remove-class "active")
                  [:li :a] (html/content text)
                  [:li :a] (html/set-attr :href href)))

(html/deftemplate index-page-template bootstrap
  [title nav-links logged-in? content-snippet]
  [:title] (html/content title)
  [:#navigation] (when logged-in? (html/substitute (navigation-snippet nav-links)))
  [:#navigation-login] (html/content (navigation-login-snippet logged-in?))
  [:#content] (html/content content-snippet))

(html/defsnippet login-form-snippet bootstrap [:#login]
  [])

(html/defsnippet validation-errors-snippet bootstrap [:#friend-form :.validation-errors]
  [errors]
  [:ul [:li (html/but html/first-of-type)]] nil
  [:ul [:li html/first-of-type]]
  (html/clone-for [text (vals errors)]
                  [:li] (html/content (str text))))

(defn transform-radio [value text]
  (fn [node]
    (let [input (-> (html/select node [:input])
                    first
                    ((html/set-attr :value (str value)))
                    ((html/set-attr :id (str "meet-freq-" value))))]
      (-> node
          ((html/content input))
          ((html/append text))
          ((html/set-attr :for (str "meet-freq-" value)))))))

(html/defsnippet friend-form-snippet bootstrap [:#friend-form]
  [posted-data errors]
  [:.validation-errors] (when-not (empty? errors)
                          (html/substitute (validation-errors-snippet errors)))
  [[:input (html/attr= :name "firstname")]] (html/set-attr :value (:firstname posted-data))
  [[:input (html/attr= :name "lastname")]] (html/set-attr :value (:lastname posted-data))
  [[:textarea (html/attr= :name "notes")]] (html/set-attr :value (:notes posted-data))
  [[:.meet-freq (html/but html/first-of-type)]] nil
  [[:.meet-freq html/first-of-type]]
  (html/clone-for [[value text] (sort-by first data/meet-freq)]
                  [:label] (transform-radio value text))
  [:.meet-freq [:input (html/attr= :value (:meet-freq posted-data))]] (html/set-attr :checked "checked"))

(html/defsnippet friend-list-snippet bootstrap [:#friend-list]
  [friends]
  [:table :tr.friend-row]
  (html/clone-for [{:keys [firstname lastname notes meet-freq id overdue-seen]} friends]
                  [:tr.friend-row :.friend-name :a]
                    (html/content (str firstname " " lastname))
                  [:tr.friend-row :.friend-name :a]
                    (html/set-attr :href (r/path :friend-details :id id))
                  [:tr.friend-row :.friend-meet-freq]
                    (html/content (get data/meet-freq meet-freq))
                  [:tr.friend-row :.friend-overdue-seen]
                    (html/content (str overdue-seen))
                  [:tr.friend-row :.friend-just-seen :button]
                    (html/set-attr :value id)
                  [:tr.friend-row :.friend-just-seen :button]
                    (html/set-attr :name "just-seen")
                  [:tr.friend-row :.friend-just-seen :form]
                    (html/set-attr :action (r/path :friend-list-update))
                  [:tr.friend-row :.friend-just-seen :form]
                    (html/set-attr :method "POST")))

(html/defsnippet friend-details-snippet bootstrap [:#friend-details]
  [{:keys [firstname lastname meet-freq notes last-seen overdue-seen]}]
  [:#friend-details-name] (html/content (str firstname " " lastname))
  [:#friend-details-meet-freq-firstname] (html/content firstname)
  [:#friend-details-meet-freq] (html/content (get data/meet-freq meet-freq))
  [:#friend-details-notes] (html/content notes)
  [:#friend-details-last-seen] (html/content (show-date last-seen))
  [:#friend-details-overdue-seen] (html/content (str overdue-seen))
  )

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
