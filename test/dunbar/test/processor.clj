(ns dunbar.test.processor
  (:require [midje.sweet :refer :all]
            [dunbar.processor :as p]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [dunbar.test.test-components :refer [new-test-clock]]
            [dunbar.test.helpers.builders :refer [build-friend]]))

(defn day [d m]
  (tc/to-long (t/date-time 2014 m d 0 0 0)))

(facts "can calculate interval since you last seeing your friend"
       (let [clock (new-test-clock (day 30 3))]
         (p/process-friend (build-friend {:last-seen (day 25 3) :created-at (day 1 1)}) clock) => (contains {:last-seen-interval 5})
         (p/process-friend (build-friend {:last-seen (day 15 3) :created-at (day 1 1)}) clock) => (contains {:last-seen-interval 15})
         (p/process-friend (build-friend {:last-seen nil :created-at (day 1 3)}) clock) => (contains {:last-seen-interval 29})
         ))

(facts "can calculate how overdue you are to see your friend"
       (let [clock (new-test-clock (day 30 3))]
         (p/process-friend (build-friend {:last-seen (day 25 3) :meet-freq 7}) clock) => (contains {:overdue-seen 0})
         (p/process-friend (build-friend {:last-seen (day 15 3) :meet-freq 7}) clock) => (contains {:overdue-seen 8})
         (p/process-friend (build-friend {:last-seen nil :created-at (day 1 3) :meet-freq 7}) clock) => (contains {:overdue-seen 22})))

(facts "processing list of friends"
       (fact "should sort processed friends by overdue-seen (desc),
              followed by firstname, then lastname"
             (let [clock (new-test-clock (day 1 5))
                   friends [(build-friend {:last-seen (day 1 4) :id 1 :meet-freq 1})
                            (build-friend {:last-seen (day 1 3) :id 2 :meet-freq 1})
                            (build-friend {:last-seen (day 27 3) :id 3 :meet-freq 1})
                            (build-friend {:last-seen (day 15 3) :id 4 :meet-freq 1 :firstname "A" :lastname "B"})
                            (build-friend {:last-seen (day 15 3) :id 5 :meet-freq 1 :firstname "A" :lastname "A"})
                            (build-friend {:last-seen (day 15 3) :id 6 :meet-freq 1 :firstname "z" :lastname "z"})]]
               (map :id (p/process-friends friends clock)) => [2 5 4 6 3 1])))
