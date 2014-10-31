(ns kerodon.stateful
  (:require [kerodon.core :as k]
            [kerodon.impl :as i]
            [net.cgrand.enlive-html :as html]))

(def ^:private state (atom {}))

(defn- sfn [f & args]
  (try
    (swap! state (fn [s] (apply f s args)))
    (catch Exception e
      (prn e))))

(defn start-session [app]
  (swap! state (constantly (k/session app))))

(defn print-state []
  (prn (:enlive @state)))

(defn follow-redirect []
  (sfn k/follow-redirect))

(defn auto-follow-redirect []
  (let [status (-> @state :response :status)
        location (-> @state (get-in [:response :headers "Location"]))]
    (when (and location (not (= status 200)))
      (do (follow-redirect) (auto-follow-redirect)))))

(defn status []
  (-> @state :response :status))

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

(defn elements [selector]
  (-> @state :enlive (html/select selector)))

(defn nodes->text [n]
  (cond
    (map? n) (nodes->text (:content n))
    (coll? n) (reduce str (map nodes->text n))
    :else n))

(defn text [selector]
  (let [t (map nodes->text (-> @state :enlive (html/select selector)))]
    (if (<= (count t) 1) (first t) t)))

(defn attribute [selector attr]
  (map (comp attr :attrs)
       (-> @state :enlive (html/select selector))))

(defn value [selector]
  (attribute selector :value))

(defn first-attribute [selector attr]
  (first (attribute selector attr)))

(defn is-checked? [selector]
  (= (-> (i/form-element-for (:enlive @state) selector) first :attrs :checked) "checked"))

(defn is-selected? [selector]
  (= (-> (i/form-element-for (:enlive @state) selector) first :attrs :selected) "selected"))

(defn field-value [selector]
  (-> (i/form-element-for (:enlive @state) selector) first :attrs :value))

(defn first-value [selector]
  (first (value selector)))

(defn page-title []
  (text [:title]))
