(ns dunbar.validation
  (:require [clj-yaml.core :as yaml]))


(defn min-length [length]
  (fn [v]
    (when (< (count v) length)
      :min-length)))

(defn max-length [length]
  (fn [v]
    (when (> (count v) length)
      :max-length)))

(defn mandatory [v]
  (when (or (and (string? v) (clojure.string/blank? v)) (nil? v))
    :mandatory))

(def validations
  {:firstname [mandatory (max-length 50)]
   :lastname [mandatory (max-length 50)]})

(defn remove-errors-if-empty [result]
  (if (empty? (:errors result))
    (dissoc result :errors)
    result))

(defn validator [validations data]
  (->
   {:errors
    (into {}
          (for [[k v] validations]
            (cond (coll? v)
                  (when-let [r (reduce (fn [result validation] (or result (validation (k data)))) nil v)]
                    [[k] r])
                  :else
                  (when-let [r (v (k data))]
                    [[k] r])
                  )))}
    (merge data)
    remove-errors-if-empty))


(defn translate-errors [errors]
  (let [t (yaml/parse-string (slurp (clojure.java.io/resource "lang/en/errors.yml")))]
    (into {}
          (for [[k v] errors]
            [[k] (-> (get-in t k) v)]))))


(defn validate [data]
  (validator validations data))

(defn validate-with-translations [data]
  (-> (validate data)
      (update-in [:errors] translate-errors)
      remove-errors-if-empty))
