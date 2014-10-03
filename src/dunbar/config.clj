(ns dunbar.config
  (:require [clj-yaml.core :refer [parse-string]]))

(defn load-config-uncached [file-path]
  (println "Loading config...")
  (-> (slurp file-path) parse-string))

(def load-config (memoize load-config-uncached))
