(ns dunbar.view
  (:require [net.cgrand.enlive-html :as html]))

(html/deftemplate index-page "public/templates/index.html"
  [title message]
  [:title] (html/content title)
  [:.message] (html/content message))
