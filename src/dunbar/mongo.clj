(ns dunbar.mongo
  (:require [com.stuartsierra.component :as component]
            [monger.core :as mongo]
            [monger.collection :as mongo-c]
            )
  )

(defprotocol DB
  (save! [this table record])
  (query [this table query])
  (query-one [this table query])
  (delete! [this table query]))

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
    (prn "Starting up MongoDB")
    (init-db this host port db-name))
  (stop [this]
    (prn "Stopping MongoDB")
    (disconnect this))
  DB
  (save! [this table record]
    (mongo-c/save-and-return (:db this) table record))
  (query [this table query]
    (mongo-c/find-maps (:db this) table query))
  (query-one [this table query]
    (throw (Exception. "Implement me")))
  (delete! [this table query]
    (throw (Exception. "Implement me"))))

(defn new-mongo-db [host port db-name]
  (MongoDB. host port db-name))
