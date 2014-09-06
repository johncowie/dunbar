(ns dunbar.models
  (:require [traversy.lens :as l]))

#_(def sample-friend
  {:firstname "Emma"
   :lastname "Hughes"
   :freq-to-see 30
   :birthday [1981 8 23]
   :last-seen [2014 8 30] })


(def sample-user
  {:username "John"
   :email "John.a.cowie@gmail.com"
   :friends []})


(defn >username [user]
  (l/view (l/in [:username]) user))
