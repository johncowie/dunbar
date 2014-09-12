(ns dunbar.view
  (:require [net.cgrand.enlive-html :as html]))

(html/deftemplate index-page-template "public/templates/index.html"
  [title content]
  [:title] (html/content title)
  [:#content] (html/html-content content))

(html/defsnippet hello-snippet "public/templates/index.html" [:#home]
  [message]
  [:.message] (html/content message))

(html/defsnippet login-form-snippet "public/templates/index.html" [:#login]
  [])

(html/defsnippet friend-form-snippet "public/templates/index.html" [:#friend-form]
  [])

(html/defsnippet friend-list-snippet "public/templates/index.html" [:#friend-list]
  [friends]
  [:table :tr.friend-row]
  (html/clone-for [{firstname :firstname lastname :lastname} friends]
                  [:tr.friend-row :.friend-name] (html/content (str firstname " " lastname))
                  [:tr.friend-row :.friend-last-seen] (html/content "Last seen not implemented yet")))

(defn- render-snippet [snippet]
  (reduce str (html/emit* snippet)))

(defn- page
  [title content]
  (reduce str (index-page-template title content)))

(defn login-form-page [title]
  (page title (render-snippet (login-form-snippet))))

(defn friend-form-page [title]
  (page title (render-snippet (friend-form-snippet))))

(defn friend-list-page [title friends]
  (page title (render-snippet (friend-list-snippet friends))))

(defn hello-page
  [title message]
  (page title (render-snippet (hello-snippet message))))
