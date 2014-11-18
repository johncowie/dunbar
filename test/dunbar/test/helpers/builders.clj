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
    :id "captain-scarlet"
    })
  ([overrides]
   (merge (build-friend) overrides)))

(defn build-twitter-user
  ([]
   {:id 13245678
    :name "Bono U2"
    :screen_name "bono"})
  ([overrides]
   (merge (build-twitter-user) overrides)))
