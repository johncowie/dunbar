(ns dunbar.store
  (:require [dunbar.mongo :refer [save! query update!]]
            [clojure.string :refer [lower-case]]))

(def friend-coll "friends")

(defn primary-key-query [friend]
  (select-keys friend [:id :user]))

(defn add-friend [friend db]
  (save! db friend-coll friend))

(defn update-friend [updated-friend db]
  (update! db friend-coll (primary-key-query updated-friend) updated-friend))

(defn load-friends [db username]
  (->>
   (query db friend-coll {:user username})
   (sort-by #(lower-case (str (:firstname %) (:lastname %))))))

(defn load-friend [db username id]
  (first (query db friend-coll {:user username :id id})))
