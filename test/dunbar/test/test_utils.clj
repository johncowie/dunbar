(ns dunbar.test.test-utils)

;; CUSTOM MATCHERS

(defn string-of-length [length]
  (reduce str (repeat length "a")))
