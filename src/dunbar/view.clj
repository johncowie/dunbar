(ns dunbar.view
  (:require [net.cgrand.enlive-html :as html]
            [dunbar.routes :as r]))

(defn- render-snippet [snippet]
  (reduce str (html/emit* snippet)))

(html/deftemplate index-page-template "public/templates/index.html"
  [title navigation-snippet content-snippet]
  [:title] (html/content title)
  [:#navigation] (html/html-content (render-snippet navigation-snippet))
  [:#content] (html/html-content (render-snippet content-snippet)))

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

(html/defsnippet friend-form-snippet "public/templates/index.html" [:#friend-form]
  [posted-data errors]
  [:.validation-errors] (when-not (empty? errors)
                          (html/substitute (validation-errors-snippet errors)))
  [[:input (html/attr= :name "firstname")]] (html/set-attr :value (:firstname posted-data))
  [[:input (html/attr= :name "lastname")]] (html/set-attr :value (:lastname posted-data))
  [[:textarea (html/attr= :name "notes")]] (html/set-attr :value (:notes posted-data))
  [[:select (html/attr= :name "meet-freq")] [:option (html/attr= :value (:meet-freq posted-data))]]
  (html/set-attr :selected "selected"))

(html/defsnippet friend-list-snippet "public/templates/index.html" [:#friend-list]
  [friends]
  [:table :tr.friend-row]
  (html/clone-for [{:keys [firstname lastname notes meet-freq]} friends]
                  [:tr.friend-row :.friend-name] (html/content (str firstname " " lastname))
                  [:tr.friend-row :.friend-notes] (html/content notes)
                  [:tr.friend-row :.friend-meet-freq] (html/content (str meet-freq))
                  ))

(defn- page
  [title nav-links content]
  (reduce str (index-page-template title (navigation-snippet nav-links) content)))

(defn login-form-page [title nav]
  (page title nav (login-form-snippet)))

(defn friend-form-page [title nav posted-data errors]
  (page title nav (friend-form-snippet posted-data errors)))

(defn friend-list-page [title nav friends]
  (page title nav (friend-list-snippet friends)))
