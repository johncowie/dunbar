(ns dunbar.test.view
  (:require [midje.sweet :refer :all]
            [net.cgrand.enlive-html :refer [html-snippet select attr=]]
                                        ;[dunbar.view :refer [css-select]]
            [dunbar.view :as v]
            ))

(defn classes [node]
  (-> node :attrs :class (clojure.string/split #"\s+") set))

(defn has-attr? [attr v]
  (fn [node]
    (= (-> node :attrs attr) v)))

(defn has-class? [class]
  (fn [node]
    (contains (classes node) class)))

(defn has-content? [text]
  (fn [node]
    (= (-> node :content first) text)))

(fact "can render navigation list"
      (let [nav-data [{:text "A" :href "/link1"}
                      {:text "B" :href "/link2" :selected true}]
            output (v/navigation-snippet nav-data)
            nav-list (select output [:li])
            links (select output [:a])]
        (first nav-list) =not=> (has-class? "active")
        (first links) => (has-content? "A")
        (first links) => (has-attr? :href "/link1")
        (second nav-list) => (has-class? "active")
        (second links) => (has-content? "B")
        (second links) => (has-attr? :href "/link2")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Experimenting with string to enlive conversion ;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn build-regex [& rs] (re-pattern (reduce str rs)))

(defn enlive-token [t]
  (-> t
      keyword))

(def token-pattern #"[.|#]?[a-z\-\_]+")

(re-matches token-pattern "a")

(defn build-AND-group [g]
  (->> g
       (re-seq token-pattern)
       (map enlive-token)
       vec))

(build-AND-group "[a.b]")

(defn tokenise-css [css]
  (->>
     (clojure.string/split css #"\s+")
     (map build-AND-group)
     vec))

(defn css-select [s]
 (tokenise-css s))

(defn can-select? [html-fragment]
  (fn [selector]
    (let [snippet (html-snippet html-fragment)
          result (select snippet selector)]
      (if (empty? result)
        (do (prn snippet) (prn result) nil)
        true))))

(css-select "title")

(select (html-snippet "<title></title>") [[:title]])

(fact "can convert strings into enlive selectors"
       (css-select "title") => (can-select? "<title></title>")
       (css-select "#an-id") => (can-select? "<div id=\"an-id\"></div>")
       (css-select ".a-class") => (can-select? "<div class=\"a-class\"></div>")
       ;(css-select "input[name=\"bob\"]") => (can-select? "<input name=\"bob\"></input>")
       ;(css-select "input[name=\"bi-ll\"]") => (can-select? "<input name=\"bi-ll\"></input>")
)
