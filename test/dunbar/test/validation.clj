(ns dunbar.test.validation
  (:require [midje.sweet :refer :all]
            [dunbar.validation :refer [validate validator mandatory]]))

(defn string-of-length [length]
  (apply str (take length (repeat "a"))))

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
       (fact "valid friend"
             (validate {:firstname "Joe" :lastname "Bloggs"})
             => {:firstname "Joe" :lastname "Bloggs"}))
