(ns dunbar.store
  (:require [dunbar.mongo :refer [save! query]]
            [clojure.string :refer [lower-case]]))

(defn add-friend [friend db]
  (save! db "friends" friend))

(defn load-friends [db username]
  (->>
   (query db "friends" {:user username})
   (sort-by #(lower-case (str (:firstname %) (:lastname %))))))

(defn load-friend [db username id]
  (first (query db "friends" {:user username :id id})))
