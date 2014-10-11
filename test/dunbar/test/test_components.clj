(ns dunbar.test.test-components
  (:require [dunbar.mongo :refer [DB save! query update!]]
            [dunbar.clock :refer [Clock now]]
            [midje.sweet :refer :all]))

;;;;;; DB ;;;;;;

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
    (throw (Exception. "Implement me")))
  (update! [this table query record]
    (swap! db-atom (fn [db] (update-in db [table] (fn [table] (map #(if (query-match % query) record %) table)))))))

(defn new-test-db []
  (TestDB. (atom {})))

(facts "about test-db"
       (facts "inserting and querying"
              (let [db (new-test-db)]
                (save! db "apples" {:id 1 :type "braeburn"})
                (save! db "apples" {:id 2 :type "granny-smith"})
                (query db "apples" {:type "braeburn"}) => [{:id 1 :type "braeburn"}]
                (query db "apples" {:type "granny-smith"}) => [{:id 2 :type "granny-smith"}]))
       (facts "updating"
              (let [db (new-test-db)]
                (save! db "apples" {:id 1 :type "blue"})
                (save! db "apples" {:id 2 :type "green"})
                (update! db "apples" {:id 1} {:id 1 :type "red"})
                (query db "apples" {:id 1}) => [{:id 1 :type "red"}]
                (query db "apples" {:id 2}) => [{:id 2 :type "green"}])))

;;;;;;; Clock ;;;;;;;;;;

(defrecord TestClock [constant-time]
  Clock
  (now [this]
    constant-time))

(defn new-test-clock [constant-time]
  (TestClock. constant-time))
