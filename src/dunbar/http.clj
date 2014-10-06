(ns dunbar.http
  (:require [clj-http.client :as http]))

(defprotocol HTTP
  (http-get [this uri])
  )

(defrecord TestHTTP [next-responses]
  HTTP
  (http-get [this uri]
    (if (empty? @next-responses)
      (throw (Exception. "no more responses defined"))
      (let [r (first @next-responses)]
        (swap! next-responses rest)
        r))))

(defrecord RealHTTP []
  HTTP
  (http-get [this uri]
    (http/get uri)))

(defn new-test-http [next-responses]
  (TestHTTP. (atom next-responses)))

(defn new-http []
  (RealHTTP.))
