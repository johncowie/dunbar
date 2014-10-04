(ns dunbar.test.functional
  (:require [midje.sweet :refer :all]
            [kerodon.core :as k]
            [dunbar.handler :refer [make-app]]
            [dunbar.test.test-components :refer [new-test-db]]
            [net.cgrand.enlive-html :as html]
            ))

(def state (atom {}))

(defn start-session [app]
  (swap! state (constantly (k/session app))))

(defn end-session []
  (swap! state (constantly {})))

(defn follow-redirect []
  (swap! state k/follow-redirect))

(defn visit [path]
  (swap! state #(k/visit % path)))

(defn fill-in [selector value]
  (swap! state #(k/fill-in % selector value)))

(defn press [selector]
  (swap! state #(k/press % selector)))

(defn follow [selector]
  (swap! state #(k/follow % selector)))

(defn text [selector]
  (map (comp first :content)
       (-> @state :enlive (html/select selector))))

(defn page-title []
  (first (text [:title])))

(defn login-to-app []
  (facts "Can login to app"
         (visit "/")
         (follow-redirect)
         (follow-redirect)
         (fill-in [[:input (html/attr-has :name "username")]] "John")
         (press "Login")
         (follow-redirect)
         (page-title) => "My friends"))

(defn add-friend [firstname lastname]
  (facts "Adding a friend"
       (follow "Add")
       (page-title) => "Add friend"
       (fill-in [[:input (html/attr-has :name "firstname")]] firstname)
       (fill-in [[:input (html/attr-has :name "lastname")]] lastname)
       (press "Add")
       (follow-redirect)))

(facts "Creating a friend"
       (start-session (make-app (new-test-db)))
       (login-to-app)
       (add-friend "Boba" "Fett")
       (add-friend "Darth" "Vadar")
       (first (text [:td.friend-name])) => "Boba Fett"
       (second (text [:td.friend-name])) => "Darth Vadar"
       )
