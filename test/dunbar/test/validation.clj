(ns dunbar.test.validation
  (:require [midje.sweet :refer :all]))

(defn string-of-length [length]
  (apply str (take length (repeat "a"))))

(defn has-error? [key error-type]
  (fn [result]
    (= (get (:errors result) key) error-type)))

(defn has-errors? [key]
  (fn [result]
    (get (:errors result) key)))

(defn min-length [length]
  (fn [v]
    (when (< (count v) length)
      :min-length)))

(defn max-length [length]
  (fn [v]
    (when (> (count v) length)
      :max-length)))

(defn mandatory [v]
  (when (nil? v)
    :mandatory))

(def validations
  {:firstname [mandatory (min-length 1) (max-length 50)]
   :lastname [mandatory (min-length 1) (max-length 50)]}
  )

(defn validator [validations data]
  {:errors
   (into {}
         (for [[k v] validations]
           (cond (coll? v)
                 (if-let [r (reduce (fn [result validation] (or result (validation (k data)))) nil v)]
                   [[k] r]
                   [])
                 :else [[k] (v (k data))]
                 )))})

(defn validate [data]
  (validator validations data))

(facts "testing validation framework"
       (validator {:age [mandatory]} {}) => {:errors {[:age] :mandatory}}
       (validator {:age mandatory} {}) => {:errors {[:age] :mandatory}}
       (future-fact
        (validator {:a {:b mandatory :c mandatory}} {}) => {:errors {[:a :b] :mandatory
                                                                     [:a :c] :mandatory}})
       )

(facts "validate add friend form data"
       (facts "firstname"
              (validate {:firstname (string-of-length 51)}) => (has-error? [:firstname] :max-length)
              (validate {:firstname ""}) => (has-error? [:firstname] :min-length)
              (validate {}) => (has-error? [:firstname] :mandatory)
              (validate {:firstname (string-of-length 50)}) =not=> (has-errors? [:firstname])
              (validate {:firstname (string-of-length 1)}) =not=> (has-errors? [:firstname]))
       (facts "lastname"
              (validate {:lastname (string-of-length 51)}) => (has-error? [:lastname] :max-length)
              (validate {:lastname ""}) => (has-error? [:lastname] :min-length)
              (validate {}) => (has-error? [:lastname] :mandatory)
              (validate {:lastname (string-of-length 50)}) =not=> (has-errors? [:lastname])
              (validate {:lastname (string-of-length 1)}) =not=> (has-errors? [:lastname]))
       )
