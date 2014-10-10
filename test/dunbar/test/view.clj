(ns dunbar.test.view
  (:require [midje.sweet :refer :all]
            [net.cgrand.enlive-html :as html]
            ;[dunbar.view :refer [css-select]]
            ))

(defn css-select [s]
  [(keyword s)]
  )

(future-fact "can convert strings into enlive selectors"
       (css-select "input") => [:input]
       (css-select "#an-id") => [:#an-id]
       (css-select ".a-class") => [:.a-class]
       (css-select "input[name=\"bob\"]") => [[:input :name (html/attr= "bob")]]
       )
