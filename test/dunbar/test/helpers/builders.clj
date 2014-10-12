(ns dunbar.test.helpers.builders
  (:require [clj-time.core :as t]
            [clj-time.coerce :as tc]))

(defn build-friend
  ([]
   {:firstname "Captain"
    :lastname "Scarlet"
    :last-seen (tc/to-long (t/date-time 2014 2 2 0 0 0))
    :meet-freq 28
    :notes "He's indistructable or indispensable or something like that"
    :created-at (tc/to-long (t/date-time 2014 1 1 0 0 0))
    })
  ([overrides]
   (merge (build-friend) overrides)))
