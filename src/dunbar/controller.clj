(ns dunbar.controller
  (:require [ring.util.response :refer [response not-found redirect]]
            [dunbar.view :refer [index-page]]))

(defn hello [{{name :name} :params}]
  (response (reduce str (index-page "Hello World!" (str "Hello cruel " name)))))

(defn hidden [_] (response "I support Cardiff City!!"))

(defn four-o-four [request] (not-found "Nothing was found :-("))

(defn login [request] (response "You need to login..."))
