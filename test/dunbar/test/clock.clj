(ns dunbar.test.clock
  (:require [midje.sweet :refer :all]
            [dunbar.clock :refer [difference-in-days]]))

(def one-day (* 24 60 60 1000))

(facts "can calculate the difference in days between two times"
       (fact "nil handling"
             (difference-in-days nil nil) => nil
             (difference-in-days 1 nil) => nil
             (difference-in-days nil 1) => nil)
       (difference-in-days 0 one-day) => 1
       (difference-in-days 0 (dec one-day)) => 0
       (difference-in-days one-day 0) => -1
       (difference-in-days 0 (* 7 one-day)) => 7
       (difference-in-days 0 (dec (* 7 one-day))) => 6)
