(ns dunbar.middleware)

(defn wrap-404 [handler not-found-handler]
  (fn [request]
    (if-let [response (handler request)]
      response
      (not-found-handler request))))

(defn wrap-error-handling [handler server-error-handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e (server-error-handler request)))))
