(ns dunbar.test.functional
  (:require [midje.sweet :refer :all]
            [kerodon.stateful :refer :all]
            [dunbar.handler :refer [make-app]]
            [dunbar.test.test-components :refer [new-test-db]]
            [dunbar.test.test-utils :as u]
            [net.cgrand.enlive-html :as html]
            ))

(def firstname-field [[:input (html/attr-has :name "firstname")]])
(def lastname-field [[:input (html/attr-has :name "lastname")]])
(def notes-field [[:textarea (html/attr-has :name "notes")]])


(defn login-to-app []
  (facts "Can login to app"
         (visit "/")
         (fill-in [[:input (html/attr-has :name "username")]] "John")
         (press "Login")
         (page-title) => "My friends"))

(defn add-friend [firstname lastname notes]
  (facts "Adding a friend"
       (follow "Add")
       (page-title) => "Add friend"
       (fill-in firstname-field firstname)
       (fill-in lastname-field lastname)
       (fill-in notes-field notes)
       (press "Add")))

(defn check-friend-row [n name notes]
  (facts "Checking friend row"
         (nth (text [:td.friend-name]) n) => name
         (nth (text [:td.friend-notes]) n) => notes))

(facts "Creating a friend"
       (start-session (make-app (new-test-db)))
       (login-to-app)
       (fact "Creating an invalid friend returns validation error"
             (add-friend (u/string-of-length 100) "Yoda" "Some notes")
             (page-title) => "Add friend"
             (first-text [:.validation-errors :li]) =not=> empty?
             (fact "form fields are repopulated with old data"
                   (first-value firstname-field) => (u/string-of-length 100)
                   (first-value lastname-field) => "Yoda"
                   (first-value notes-field) => "Some notes"))
       (add-friend "Boba" "Fett" "Bounty Hunter")
       (add-friend "Darth" "Vadar" "Breathy")
       (check-friend-row 0 "Boba Fett" "Bounty Hunter")
       (check-friend-row 1 "Darth Vadar" "Breathy"))
