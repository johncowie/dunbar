(ns dunbar.test.functional
  (:require [midje.sweet :refer :all]
            [kerodon.stateful :refer :all]
            [dunbar.handler :refer [make-app]]
            [dunbar.clock :refer [date-time-millis]]
            [dunbar.test.test-components :refer [new-test-db new-test-clock adjust]]
            [dunbar.test.test-utils :as u]
            [net.cgrand.enlive-html :as html]
            [clj-time.coerce :as tc]
            [clj-time.core :as t]))

(defn test-app
  ([clock] (make-app (new-test-db) clock))
  ([] (test-app (new-test-clock 0))))

(defn input-selector [tag n]
  [[tag (html/attr-has :name n)]])

(def firstname-field (input-selector :input "firstname"))
(def lastname-field (input-selector :input "lastname"))
(def notes-field (input-selector :textarea "notes"))

(defn login-to-app []
  (facts "Can login to app"
         (visit "/")
         (fill-in "Please enter your username" "John")
         (press "Login")
         (page-title) => "My friends"))

(defn add-friend [firstname lastname notes meet-freq]
  (facts (str "Adding a friend with name " firstname " " lastname)
       (follow "Add")
       (page-title) => "Add friend"
       (fill-in "First name:" firstname)
       (fill-in "Last name:" lastname)
       (fill-in "Notes:" notes)
       (check meet-freq)
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
             (add-friend (u/string-of-length 100) "Yoda" "Some notes" "once a week")
             (page-title) => "Add friend"
             (first-text [:.validation-errors :li]) =not=> empty?
             (fact "form fields are repopulated with old data"
                   (field-value "First name:") => (u/string-of-length 100)
                   (field-value "Last name:") => "Yoda"
                   (field-value "Notes:") => "Some notes"
                   (is-checked? "once a week") => true))
       (add-friend "Boba" "Fett" "Bounty Hunter" "once a week")
       (add-friend "Darth" "Vadar" "Breathy" "once a month")
       (check-friend-row 0 "Boba Fett" "once a week")
       (check-friend-row 1 "Darth Vadar" "once a month")
       (check-friend-details "Boba" "Fett" "Bounty Hunter" "once a week")
       (check-friend-details "Darth" "Vadar" "Breathy" "once a month"))

(facts "About when you've just seen a friend"
       (let [clock (new-test-clock (date-time-millis 2014 3 25))]
         (start-session (test-app clock))
         (login-to-app)
         (add-friend "Anakin" "Skywalker" "a kid" "once a week")
         (follow "Friends")
         (first-text [:.friend-overdue-seen]) => "0"
         (follow "Anakin Skywalker")
         (first-text [:#friend-details-last-seen]) => "-"
         (follow "Friends")
         (press "Just seen them")
         (follow "Anakin Skywalker")
         (first-text [:#friend-details-last-seen]) => "25 MAR 2014"
         (adjust clock (date-time-millis 2014 4 3))
         (follow "Friends")
         (first-text [:.friend-overdue-seen]) => "2"))

(facts "General hygiene stuff"
       (start-session (test-app))
       (fact "can generate 404 page"
             (visit "/blah")
             (page-title) => "Nothing to see here.."
             (status) => 404)
       (facts "navigation"
              (fact "can only see navigation if logged in"
                    (follow "Friends") => nil
                    (follow "Add") => nil
                    (login-to-app)
                    (follow "Friends") =not=> nil
                    (follow "Add") =not=> nil)))
