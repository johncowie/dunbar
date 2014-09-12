(ns dunbar.store
  (:require [dunbar.mongo :refer [save! query]])
  )

(defn add-friend [friend db username]
  (->>
   (assoc friend :user username)
   (save! db "friends")))

(defn load-friends [db username]
  (query db "friends" {:user username}))
