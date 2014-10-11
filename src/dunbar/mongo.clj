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

(defn init-db [this host port db-name]
  (let [connection (mongo/connect {:host host :port port})]
    (-> this
        (assoc :connection connection)
        (assoc :db (mongo/get-db connection db-name)))))

(defn disconnect [this]
  (mongo/disconnect (:connection this))
  (dissoc this :connection :db))

(defrecord MongoDB [host port db-name]
  component/Lifecycle
  (start [this]
    (println "Starting up MongoDB")
    (init-db this host port db-name))
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

(defn new-mongo-db [{{host :host port :port db-name :db} :mongo}]
  (MongoDB. host port db-name))
