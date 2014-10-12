(ns dunbar.clock
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]))

(defprotocol Clock
  (now [this]))

(defrecord JodaClock []
  Clock
  (now [this]
    (c/to-long (t/now))))

(defn new-joda-clock []
  (JodaClock. ))

(defn difference-in-days [time-1-millis time-2-millis]
  (when (and time-1-millis time-2-millis)
    (let [t1 (c/from-long time-1-millis)
          t2 (c/from-long time-2-millis)]
      (if (t/before? t1 t2)
        (t/in-days (t/interval t1 t2))
        (* -1 (t/in-days (t/interval t2 t1)))))))

(defn date-time-millis [& args]
  (c/to-long (apply t/date-time args)))
