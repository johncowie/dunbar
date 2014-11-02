(ns dunbar.test.store
  (:require [midje.sweet :refer :all]
            [dunbar.store :as s]
            [dunbar.components.stubs :refer [new-test-db]]))

(fact "Can add and load friends by username"
      (let [db (new-test-db)]
        (s/add-friend {:firstname "John" :lastname "Doe" :user "derek"} db)
        (s/load-friends db "derek") => [{:firstname "John" :lastname "Doe" :id "john-doe" :user "derek"}])
      (facts "Friends are returned in alphabetical order, by firstname then lastname (case-insensitive)"
             (let [db (new-test-db)]
               (s/add-friend {:firstname "B" :lastname "A" :user "x"} db)
               (s/add-friend {:firstname "A" :lastname "B" :user "x"} db)
               (s/add-friend {:firstname "A" :lastname "A" :user "x"} db)
               (s/add-friend {:firstname "a" :lastname "c" :user "x"} db)
               (s/load-friends db "x") => [{:firstname "A" :lastname "A" :user "x" :id "a-a"}
                                           {:firstname "A" :lastname "B" :user "x" :id "a-b"}
                                           {:firstname "a" :lastname "c" :user "x" :id "a-c"}
                                           {:firstname "B" :lastname "A" :user "x" :id "b-a"}])))

(fact "Can update a friend"
      (let [db (new-test-db)]
        (s/add-friend {:firstname "Bill" :lastname "Haley" :user "x"} db)
        (s/update-friend {:firstname "Bill" :lastname "Kill" :id "bill-haley" :user "x"} db)
        (s/load-friend db "x" "bill-haley") => {:firstname "Bill" :lastname "Kill" :id "bill-haley" :user "x"}))

(fact "Can load friend by username and id"
      (let [db (new-test-db)]
        (s/add-friend {:firstname "John" :lastname "Doe" :user "derek"} db)
        (s/add-friend {:firstname "John" :lastname "Doe" :user "dilbert"} db)
        (s/load-friend db "derek" "john-doe") => {:firstname "John" :lastname "Doe" :id "john-doe" :user "derek"}
        (s/load-friend db "dilbert" "john-doe") => {:firstname "John" :lastname "Doe" :id "john-doe" :user "dilbert"}))

(fact "Can generate ID for a friend"
      (let [db (new-test-db)]
        (s/generate-id db {:firstname "John" :lastname "Doe"}) => "john-doe"
        (s/generate-id db {:firstname "P!unc-  tu ,.  A'tion0 " :lastname " man "}) => "punc-tu-ation0-man"
        (future-fact "If id already exists, then appends number to it"
              (s/add-friend {:firstname "A" :lastname "B" :user "x"} db)
              (s/load-friends db "x") => [{:firstname "A" :lastname "B" :user "x" :id "a-b"}]
              (s/add-friend {:firstname "A" :lastname "B" :user "x"} db)
              (s/load-friends db "x") => [{:firstname "A" :lastname "B" :user "x" :id "a-b"}
                                          {:firstname "A" :lastname "B" :user "x" :id "a-b-2"}]
              (s/add-friend {:firstname "A" :lastname "B" :user "x"} db)
              (s/load-friends db "x") => [{:firstname "A" :lastname "B" :user "x" :id "a-b"}
                                          {:firstname "A" :lastname "B" :user "x" :id "a-b-2"}
                                          {:firstname "A" :lastname "B" :user "x" :id "a-b-3"}])))
