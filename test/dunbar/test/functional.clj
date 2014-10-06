(ns dunbar.test.functional
  (:require [midje.sweet :refer :all]
            [kerodon.stateful :refer :all]
            [dunbar.handler :refer [make-app]]
            [dunbar.test.test-components :refer [new-test-db]]
            [dunbar.test.test-utils :as u]
            [net.cgrand.enlive-html :as html]
            ))

(defn input-selector [tag name]
  [[tag (html/attr-has :name name)]])

(def firstname-field (input-selector :input "firstname"))
(def lastname-field (input-selector :input "lastname"))
(def notes-field (input-selector :textarea "notes"))
(def meet-freq-select (input-selector :select "meet-freq"))

(defn login-to-app []
  (facts "Can login to app"
         (visit "/")
         (fill-in [[:input (html/attr-has :name "username")]] "John")
         (press "Login")
         (page-title) => "My friends"))

(defn add-friend [firstname lastname notes meet-freq]
  (facts "Adding a friend"
       (follow "Add")
       (page-title) => "Add friend"
       (fill-in firstname-field firstname)
       (fill-in lastname-field lastname)
       (fill-in notes-field notes)
       (choose meet-freq-select meet-freq)
       (press "Add")))

(defn check-friend-row [n name notes meet-freq]
  (facts "Checking friend row"
         (page-title) => "My friends"
         (nth (text [:td.friend-name]) n) => name
         (nth (text [:td.friend-notes]) n) => notes
         (nth (text [:td.friend-meet-freq]) n) => meet-freq))

(facts "Creating a friend"
       (start-session (make-app (new-test-db)))
       (login-to-app)
       (fact "Creating an invalid friend returns validation error"
             (add-friend (u/string-of-length 100) "Yoda" "Some notes" "7")
             (page-title) => "Add friend"
             (first-text [:.validation-errors :li]) =not=> empty?
             (fact "form fields are repopulated with old data"
                   (first-value firstname-field) => (u/string-of-length 100)
                   (first-value lastname-field) => "Yoda"
                   (first-value notes-field) => "Some notes"
                   (selected-value meet-freq-select) => "7"
                   ))
       (add-friend "Boba" "Fett" "Bounty Hunter" "7")
       (add-friend "Darth" "Vadar" "Breathy" "7")
       (check-friend-row 0 "Boba Fett" "Bounty Hunter" "7")
       (check-friend-row 1 "Darth Vadar" "Breathy" "7"))
