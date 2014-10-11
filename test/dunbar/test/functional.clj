(ns dunbar.test.functional
  (:require [midje.sweet :refer :all]
            [kerodon.stateful :refer :all]
            [dunbar.handler :refer [make-app]]
            [dunbar.test.test-components :refer [new-test-db new-test-clock]]
            [dunbar.test.test-utils :as u]
            [net.cgrand.enlive-html :as html]
            [clj-time.coerce :as tc]
            [clj-time.core :as t]))

(defn test-app
  ([t] (make-app (new-test-db) (new-test-clock t)))
  ([] (test-app 0)))

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

(defn check-friend-row [n name meet-freq]
  (facts "Checking friend row"
         (page-title) => "My friends"
         (nth (text [:td.friend-name :a]) n) => name
         (nth (text [:td.friend-meet-freq]) n) => meet-freq))

(defn check-friend-details [firstname lastname notes meet-freq]
  (facts "Checking friend details"
         (follow "Friends")
         (follow (str firstname " " lastname))
         (page-title) => (str firstname " " lastname)
         (first-text [:#friend-details-name]) => (str firstname " " lastname)
         (first-text [:#friend-details-meet-freq-firstname]) => firstname
         (first-text [:#friend-details-meet-freq]) => meet-freq
         (first-text [:#friend-details-notes]) => notes))

(facts "Creating a friend"
       (start-session (test-app))
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
       (check-friend-row 0 "Boba Fett" "7")
       (check-friend-row 1 "Darth Vadar" "7")
       (check-friend-details "Boba" "Fett" "Bounty Hunter" "7")
       (check-friend-details "Darth" "Vadar" "Breathy" "7"))

(facts "About when you've just seen a friend"
       (start-session (test-app (tc/to-long (t/date-time 2014 03 25))))
       (login-to-app)
       (add-friend "Anakin" "Skywalker" "a kid" "7")
       (follow "Friends")
       (follow "Anakin Skywalker")
       (first-text [:#friend-details-last-seen]) => "-"
       (follow "Friends")
       (press "Just seen them")
       (follow "Anakin Skywalker")
       (first-text [:#friend-details-last-seen]) => "25 MAR 2014")

(facts "General hygiene stuff"
       (start-session (test-app))
       (fact "can generate 404 page"
             (visit "/blah")
             (page-title) => "Nothing to see here.."
             (status) => 404)
       (facts "navigation"
              (fact "can only see navigation if logged in"
                    (follow "Friends") => (throws Exception)
                    (follow "Add") => (throws Exception)
                    (login-to-app)
                    (follow "Friends") =not=> (throws Exception)
                    (follow "Add") =not=> (throws Exception))))
