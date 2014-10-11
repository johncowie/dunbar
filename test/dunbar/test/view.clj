(ns dunbar.test.view
  (:require [midje.sweet :refer :all]
            [net.cgrand.enlive-html :refer [html-snippet select attr=]]
            ;[dunbar.view :refer [css-select]]
            ))

(defn build-regex [& rs] (re-pattern (reduce str rs)))
(def r-token #"[a-zA-Z-_]")
(def r-attr #"[a-zA-Z]")
(def r-tag #"[a-zA-Z]")
(def r-id #"#[a-zA-Z-_]")
(def r-class #".[a-zA-Z-_]")

;(defn attribute [css-selector]
 ; (let [[match element attr v] (re-matches tag-with-attribute css-selector)]
  ;  (when match
   ;   [(keyword element) (html/attr= (keyword attr) v)])))

(defn basic-selector [css-selector]
  (when-not (empty? css-selector)
    (keyword css-selector)))

(defn css-select [s]
  [((some-fn basic-selector) s)])

(defn can-select? [html-fragment]
  (fn [selector]
    (let [snippet (html-snippet html-fragment)
          result (select snippet selector)]
      (if (empty? result)
        (do (prn snippet) (prn result) nil)
        true))))

(future-fact "can convert strings into enlive selectors"
       (css-select "title") => (can-select? "<title></title>")
       (css-select "#an-id") => (can-select? "<div id=\"an-id\"></div>")
       (css-select ".a-class") => (can-select? "<div class=\"a-class\"></div>")
       (css-select "input[name=\"bob\"]") => (can-select? "<input name=\"bob\"></input>")
       (css-select "input[name=\"bi-ll\"]") => (can-select? "<input name=\"bi-ll\"></input>")
     )
