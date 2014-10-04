(ns dunbar.logic)

(defn process [{:keys [success state]} f]
  (if success
    (let [new-state (f state)]
      {:success (empty? (:errors new-state)) :state new-state})
    {:success success :state state}))

(defn error-> [state & fs]
  (reduce process {:success true :state state} fs))
