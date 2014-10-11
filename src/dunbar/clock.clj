(ns dunbar.clock)

(defprotocol Clock
  (now [this]))

(defrecord JodaClock []
  Clock
  (now [this]
    (throw (Exception. "Implement me"))))
