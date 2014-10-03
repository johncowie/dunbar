(ns dunbar.test.models
  (:require [midje.sweet :refer :all]
            [dunbar.models :as m]
            [traversy.lens :refer [view]]))

(facts "user lenses"
       (future-fact "username"
                    (-> m/sample-user (view m/>username)) => "John"))
