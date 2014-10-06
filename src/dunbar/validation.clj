(ns dunbar.validation
  (:require [clj-yaml.core :as yaml]))


(defn min-length [length]
  (fn [v]
    (when (and (string? v) (< (count v) length))
      :min-length)))

(defn max-length [length]
  (fn [v]
    (when (and (string? v) (> (count v) length))
      :max-length)))

(defn mandatory [v]
  (when (or (and (string? v) (clojure.string/blank? v)) (nil? v))
    :mandatory))

(defn optional [v]
  (when (or (and (string? v) (clojure.string/blank? v)) (nil? v))
    :valid))

(defn numeric [v]
  (try
    (do
      (Integer/parseInt v)
      nil)
    (catch Exception e :numeric)))

(defn positive [v]
  (if (<= (Integer/parseInt v) 0)
    :positive))

(defn remove-errors-if-empty [result]
  (if (empty? (:errors result))
    (dissoc result :errors)
    result))

(defn process-validation-list [value v]
  (let [status (cond (coll? v)
                     (reduce (fn [result validation] (or result (validation value))) nil v)
                     :else
                     (v value))]
    (when-not (= status :valid)
      status)))

(defn validator [validations data]
  (->
   {:errors
    (into {}
          (for [[k v] validations]
            (when-let [r (process-validation-list (k data) v)]
              [[k] r])
            ))}
    (merge data)
    remove-errors-if-empty))


(defn translate-errors [errors]
  (let [get-translation (fn [t k v] (if-let [r (-> (get-in t k) v)] r (throw (Exception. (format "No error translation for path %s" (vec (conj k v)))))))
        t (yaml/parse-string (slurp (clojure.java.io/resource "lang/en/errors.yml")))]
    (into {}
          (for [[k v] errors]
            [[k] (get-translation t k v)]))))



;; NON-LIBRARY-BIT (FRIEND-SPECIFIC VALIDATIONS)

(def validations
  {:firstname [mandatory (max-length 50)]
   :lastname [mandatory (max-length 50)]
   :notes [optional (max-length 1000)]
   :meet-freq [mandatory numeric positive]
   })


(defn validate [data]
  (validator validations data))

(defn validate-with-translations [data]
  (-> (validate data)
      (update-in [:errors] translate-errors)
      remove-errors-if-empty))
