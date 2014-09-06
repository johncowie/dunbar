(ns dunbar.test.models
  (:require [midje.sweet :refer :all]
            [dunbar.models :as m]
            [traversy.lens :refer [view-all]]))

(facts "user lenses"
       (-> m/sample-user (view-all m/>username)) => "John")
