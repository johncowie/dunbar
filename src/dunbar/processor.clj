(ns dunbar.processor
  (:require [dunbar.clock :refer [difference-in-days now]]))

(defn last-seen-interval [stats-so-far {:keys [last-seen created-at]} clock]
  (assoc stats-so-far :last-seen-interval (difference-in-days (or last-seen created-at) (now clock))))

(defn overdue-to-see [stats-so-far {meet-freq :meet-freq}]
   (let [i (- (:last-seen-interval stats-so-far) meet-freq)]
      (assoc stats-so-far :overdue-seen (if (neg? i) 0 i))))

(defn process-friend [friend clock]
  (-> friend
      (last-seen-interval friend clock)
      (overdue-to-see friend)))

(defn process-friends [friends clock]
  (->> friends
    (map #(process-friend % clock))
    (sort-by #(clojure.string/lower-case (str (:firstname %) (:lastname %))))
    reverse
    (sort-by :overdue-seen)
    reverse))
