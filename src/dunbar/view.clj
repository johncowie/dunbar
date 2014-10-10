(ns dunbar.view
  (:require [net.cgrand.enlive-html :as html]
            [dunbar.routes :as r]))

(defn css-select [s]
  [(keyword s)]
  )

(html/deftemplate index-page-template "public/templates/index.html"
  [title navigation-snippet navigation-login-snippet content-snippet]
  [:title] (html/content title)
  [:#navigation] (html/substitute navigation-snippet)
  [:#navigation-login] (html/substitute navigation-login-snippet)
  [:#content] (html/content content-snippet))

; TODO combine these two parts into one top-bar snippet
(html/defsnippet navigation-login-snippet "public/templates/index.html" [:#navigation-login]
  [logged-in?]
  [:ul [:li html/first-of-type] :a] (html/content (if logged-in? "Logout" "Login"))
  [:ul [:li html/first-of-type] :a] (html/set-attr :href (r/path (if logged-in? :logout :login)))
  )

(html/defsnippet navigation-snippet "public/templates/index.html" [:#navigation]
  [nav-links]
  [:ul [:li (html/but html/first-of-type)]] nil ; remove all but first dummy link
  [:ul [:li html/first-of-type]]
  (html/clone-for [{href :href text :text} nav-links]
                  [:li :a] (html/content text)
                  [:li :a] (html/set-attr :href href)))

(html/defsnippet login-form-snippet "public/templates/index.html" [:#login]
  [])

(html/defsnippet validation-errors-snippet "public/templates/index.html" [:#friend-form :.validation-errors]
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

(html/defsnippet friend-form-snippet "public/templates/index.html" [:#friend-form]
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

(html/defsnippet friend-list-snippet "public/templates/index.html" [:#friend-list]
  [friends]
  [:table :tr.friend-row]
  (html/clone-for [{:keys [firstname lastname notes meet-freq id]} friends]
                  [:tr.friend-row :.friend-name :a] (html/content (str firstname " " lastname))
                  [:tr.friend-row :.friend-name :a] (html/set-attr :href (r/path :friend-details :id id))
                  [:tr.friend-row :.friend-notes] (html/content notes)
                  [:tr.friend-row :.friend-meet-freq] (html/content (str meet-freq))
                  ))

(html/defsnippet friend-details-snippet "public/templates/index.html" [:#friend-details]
  [{:keys [firstname lastname meet-freq notes]}]
  [:#friend-details-name] (html/content (str firstname " " lastname))
  [:#friend-details-meet-freq-firstname] (html/content firstname)
  [:#friend-details-meet-freq] (html/content (str meet-freq))
  [:#friend-details-notes] (html/content notes)
  )

(defn- page
  [title nav-links logged-in? content]
  (reduce str (index-page-template title (navigation-snippet nav-links) (navigation-login-snippet logged-in?) content)))

(defn login-form-page [title]
  (page title [] false (login-form-snippet)))

(defn friend-form-page [title nav posted-data errors]
  (page title nav true (friend-form-snippet posted-data errors)))

(defn friend-list-page [title nav friends]
  (page title nav true (friend-list-snippet friends)))

(defn friend-details-page [title nav friend]
  (page title nav true (friend-details-snippet friend)))
