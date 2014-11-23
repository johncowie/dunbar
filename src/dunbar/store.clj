(ns dunbar.store
  (:require [dunbar.mongo :refer [save! query update! delete!]]
            [clojure.string :as s]))

(def friend-coll "friends")

(defn unpunctuate-name [n]
  (-> n s/trim s/lower-case (s/replace #"\s+" "-") (s/replace #"[^a-z0-9\-]" "") (s/replace #"\-+" "-")))

(defn generate-id [db {:keys [firstname lastname user]}]
  (format "%s-%s" (unpunctuate-name firstname) (unpunctuate-name lastname)))

(defn primary-key-query [friend]
  (select-keys friend [:id :user]))

(defn add-friend [friend db]
  (->>
    (assoc friend :id (generate-id db friend))
    (save! db friend-coll)))

(defn update-friend [updated-friend db]
  (update! db friend-coll (primary-key-query updated-friend) updated-friend))

(defn load-friends [db username]
  (->>
   (query db friend-coll {:user username})
   (sort-by :id)))

(defn load-friend [db username id]
  (first (query db friend-coll {:user username :id id})))

(defn remove-friend [db username id]
  (delete! db friend-coll {:user username :id id}))
