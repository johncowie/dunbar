(ns dunbar.components.stubs
  (:require [dunbar.mongo :refer [DB save! query update!]]
            [dunbar.clock :refer [Clock now]]))

;; DB

(defn entry-match [record [k v]]
  (= (get record k) v))

(defn query-match [record q]
  (reduce #(and %1 (entry-match record %2)) true q))

(defrecord TestDB [db-atom]
  DB
  (save! [this table record]
    (swap! db-atom (fn [db] (update-in db [table] #(conj % record)))))
  (query [this table query]
    (filter #(query-match % query) (get @db-atom table)))
  (query-one [this table query]
    (throw (Exception. "Implement me")))
  (delete! [this table query]
    ;(swap! db-atom (fn [db] ()))
    )
  (update! [this table query record]
    (swap! db-atom (fn [db] (update-in db [table] (fn [table] (map #(if (query-match % query) record %) table)))))))

(defn new-test-db []
  (TestDB. (atom {})))

;; Clock

(defprotocol ITestClock
  (adjust [this new-time]))

(defrecord TestClock [time-atom]
  Clock
  (now [this]
    @time-atom)
  ITestClock
  (adjust [this new-time]
    (swap! time-atom (constantly new-time))))

(defn new-test-clock [t]
  (TestClock. (atom t)))
