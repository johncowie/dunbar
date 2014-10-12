(ns dunbar.test.validation
  (:require [midje.sweet :refer :all]
            [dunbar.validation :refer [validate validator validate-with-translations mandatory]]
            [dunbar.test.test-utils :refer [string-of-length]]))

(defn has-error? [key error-type]
  (fn [result]
    (= (get (:errors result) key) error-type)))

(defn has-errors? [key]
  (fn [result]
    (get (:errors result) key)))

(facts "testing validation framework"
       (validator {:age [mandatory]} {:a 1}) => {:a 1 :errors {[:age] :mandatory}}
       (validator {:age mandatory} {}) => {:errors {[:age] :mandatory}}
       (future-fact ""
        (validator {:a {:b mandatory :c mandatory}} {}) => {:errors {[:a :b] :mandatory
                                                                     [:a :c] :mandatory}})
       )

(facts "validate add friend form data"
       (facts "firstname"
              (validate {:firstname (string-of-length 51)}) => (has-error? [:firstname] :max-length)
              (validate {:firstname ""}) => (has-error? [:firstname] :mandatory)
              (validate {}) => (has-error? [:firstname] :mandatory)
              (validate {:firstname (string-of-length 50)}) =not=> (has-errors? [:firstname])
              (validate {:firstname (string-of-length 1)}) =not=> (has-errors? [:firstname]))
       (facts "lastname"
              (validate {:lastname (string-of-length 51)}) => (has-error? [:lastname] :max-length)
              (validate {:lastname ""}) => (has-error? [:lastname] :mandatory)
              (validate {}) => (has-error? [:lastname] :mandatory)
              (validate {:lastname (string-of-length 50)}) =not=> (has-errors? [:lastname])
              (validate {:lastname (string-of-length 1)}) =not=> (has-errors? [:lastname]))
       (facts "notes"
              (validate {:notes ""}) =not=> (has-errors? [:notes])
              (validate {}) =not=> (has-errors? [:notes])
              (validate {:notes (string-of-length 1001)}) => (has-error? [:notes] :max-length))
       (facts "meet-freq"
              (validate {}) => (has-error? [:meet-freq] :mandatory)
              (validate {:meet-freq ""}) => (has-error? [:meet-freq] :mandatory)
              (validate {:meet-freq "a"}) => (has-error? [:meet-freq] :numeric)
              (validate {:meet-freq "-1"}) => (has-error? [:meet-freq] :positive)
              (validate {:meet-freq "2"}) => (has-error? [:meet-freq] :invalid)
              (validate {:meet-freq "1"}) =not=> (has-errors? [:meet-freq]))
       (fact "valid friend"
             (validate {:firstname "Joe" :lastname "Bloggs" :notes "Some notes" :meet-freq "7"})
             => {:firstname "Joe" :lastname "Bloggs" :notes "Some notes" :meet-freq "7"}))

; Searching for missing translations

(defn generate-rubbish-value []
  (let [r (rand)]
    (cond (< r 0.3) nil
          (< r 0.6) (string-of-length (rand-int 10000))
          :else (str (- (rand-int 10000) 5000)))))

(defn generate-rubbish-friend-data []
  (zipmap [:firstname :lastname :meet-freq :notes] (repeatedly generate-rubbish-value)))

(defn attempt-translation []
  (try
    (do (validate-with-translations (generate-rubbish-friend-data)) nil)
    (catch Exception e
      (str e))))

(facts "should not be able to generate missing translation errors"
       (filter boolean (set (take 100 (repeatedly attempt-translation)))) => [])
