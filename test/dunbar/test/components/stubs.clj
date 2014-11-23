(ns dunbar.test.components.stubs
  (:require [midje.sweet :refer :all]
            [dunbar.components.stubs :refer [new-test-db]]
            [dunbar.mongo :refer [save! query update! delete!]]))

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
                (query db "apples" {:id 2}) => [{:id 2 :type "green"}]))
       (future-fact "deleting"
              (let [db (new-test-db)]
                (save! db "apples" {:id 1 :type "blue"})
                (save! db "apples" {:id 2 :type "green"})
                (delete! db "apples" {:id 1})
                (query db "apples" {:id 1}) => []
                (query db "apples" {:id 2}) => [{:id 2 :type "green"}])
              )
       )
