(ns dunbar.test.store
  (:require [midje.sweet :refer :all]
            [dunbar.store :as s]
            [dunbar.test.test-components :refer [new-test-db]]))

(fact "Can add and load friends by username"
      (let [db (new-test-db)]
        (s/add-friend {:firstname "John" :lastname "Doe"} db "derek")
        (s/load-friends db "derek") => [{:firstname "John" :lastname "Doe" :user "derek"}])
      (facts "Friends are returned in alphabetical order, by firstname then lastname (case-insensitive)"
             (let [db (new-test-db)]
               (s/add-friend {:firstname "B" :lastname "A"} db "x")
               (s/add-friend {:firstname "A" :lastname "B"} db "x")
               (s/add-friend {:firstname "A" :lastname "A"} db "x")
               (s/add-friend {:firstname "a" :lastname "c"} db "x")
               (s/load-friends db "x") => [{:firstname "A" :lastname "A" :user "x"}
                                           {:firstname "A" :lastname "B" :user "x"}
                                           {:firstname "a" :lastname "c" :user "x"}
                                           {:firstname "B" :lastname "A" :user "x"}])))
