(ns dunbar.mongo
  (:require [com.stuartsierra.component :as component]
            [monger.core :as mongo]
            [monger.collection :as mongo-c]))

(defprotocol DB
  (save! [this table record])
  (query [this table query])
  (query-one [this table query])
  (delete! [this table query])
  (update! [this table query record]))

(defn init-db [this uri]
  (let [{:keys [conn db]} (mongo/connect-via-uri uri)]
    (-> this
        (assoc :connection conn)
        (assoc :db db))))

(defn disconnect [this]
  (mongo/disconnect (:connection this))
  (dissoc this :connection :db))

(defrecord MongoDB [uri]
  component/Lifecycle
  (start [this]
    (println "Starting up MongoDB")
    (init-db this uri))
  (stop [this]
    (println "Stopping MongoDB")
    (disconnect this))
  DB
  (save! [this table record]
    (mongo-c/save-and-return (:db this) table record))
  (query [this table query]
    (mongo-c/find-maps (:db this) table query))
  (query-one [this table query]
    (throw (Exception. "Implement me")))
  (delete! [this table query]
    (throw (Exception. "Implement me")))
  (update! [this table query record]
    (mongo-c/update (:db this) table query record)))

(defn new-mongo-db [uri]
  (MongoDB. uri))
