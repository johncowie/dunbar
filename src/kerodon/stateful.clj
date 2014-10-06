(ns kerodon.stateful
  (:require [kerodon.core :as k]
            [net.cgrand.enlive-html :as html]))

(def ^:private state (atom {}))

(defn- sfn [f & args]
  (swap! state (fn [s] (apply f s args))))

(defn start-session [app]
  (swap! state (constantly (k/session app))))

(defn end-session []
  (swap! state (constantly {})))

(defn follow-redirect []
  (sfn k/follow-redirect))

(defn auto-follow-redirect []
  (let [status (-> @state :response :status)
        location (-> @state (get-in [:response :headers "Location"]))]
    (when (and location (not (= status 200)))
      (do (follow-redirect) (auto-follow-redirect)))))

(defn visit [path]
  (sfn k/visit path)
  (auto-follow-redirect))

(defn fill-in [selector value]
  (sfn k/fill-in selector value))

(defn press [selector]
  (sfn k/press selector)
  (auto-follow-redirect))

(defn follow [selector]
  (sfn k/follow selector))

(defn check [selector]
  (sfn k/check selector))

(defn choose [selector value]
  (sfn k/choose selector value))

(defn text [selector]
  (map (comp first :content)
       (-> @state :enlive (html/select selector))))

(defn first-text [selector]
  (first (text selector)))

(defn value [selector]
  (map (comp :value :attrs)
       (-> @state :enlive (html/select selector))))

(defn first-value [selector]
  (first (value selector)))

(defn page-title []
  (first (text [:title])))
