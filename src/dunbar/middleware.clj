(ns dunbar.middleware
  (:require [ring.util.response :refer [resource-response]]))

(defn wrap-404 [handler not-found-handler]
  (fn [request]
    (if-let [response (handler request)]
      response
      (not-found-handler request))))
