(ns dunbar.utils
  (:require [bidi.bidi :as bidi]))

; BIDI routing utils ;TODO modify to work with vectors as well
(defn attach-handlers [routes handlers]
  "swaps route keywords with corresponding functions in handler map"
  (cond (map? routes)
        (into {} (map (fn [[k v]] [k (attach-handlers v handlers)]) routes))
        (vector? routes)
        (into [] (flatten (map (fn [[k v]] [k (attach-handlers v handlers)]) (partition 2 routes))))
    (keyword? routes) (get handlers routes)
    :else routes)) ; TODO FIX  - maybe add middleware to resolve handlers and routes?

; maybe make a custom handler function
; have a look at the bidi-source-code

(defn bidi-handler [routes handlers]
  (bidi/make-handler (attach-handlers routes handlers)))

(defn path-to [routes handler & params]
  (apply bidi/path-for routes handler params))

(defn wrap-handlers
  [handlers handler-keys wrap-fn]
  (reduce #(update-in %1 [%2] wrap-fn) handlers handler-keys))
