(ns dunbar.view
  (:require [net.cgrand.enlive-html :as html]))

(html/deftemplate index-page-template "public/templates/index.html"
  [title content]
  [:title] (html/content title)
  [:#content] (html/html-content content))

(defn- page
  [title content]
  (reduce str (index-page-template title content)))

(html/defsnippet login-form-snippet "public/templates/index.html" [:#login]
  []
  )

(defn render-snippet [snippet]
  (reduce str (html/emit* snippet)))

(defn login-form-page [title]
  (page title (render-snippet (login-form-snippet))))

(html/defsnippet hello-snippet "public/templates/index.html" [:#home]
  [message]
  [:.message] (html/content message))

(defn hello-page
  [title message]
  (page title (render-snippet (hello-snippet message))))


#_(index-page "a" "hello")
#_(reduce str (html/emit* (login-form-snippet)))
