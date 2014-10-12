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

(defn input-selector [tag n]
  [[tag (html/attr-has :name n)]])

(defn radio-selector [n v]
  [:input (html/attr= :type "radio" :name n :value v)])

(def firstname-field (input-selector :input "firstname"))
(def lastname-field (input-selector :input "lastname"))
(def notes-field (input-selector :textarea "notes"))
(defn meet-freq-radio [v]
  (radio-selector "meet-freq" v))

(->
(html/html-snippet "<input type=\"radio\" value=\"1\" name=\"meet-freq\"></input>")
 (html/select (meet-freq-radio "1"))
 )

(defn login-to-app []
  (facts "Can login to app"
         (visit "/")
         (fill-in [[:input (html/attr-has :name "username")]] "John")
         (press "Login")
         (page-title) => "My friends"))

(defn add-friend [firstname lastname notes meet-freq]
  (facts (str "Adding a friend with name " firstname " " lastname)
       (follow "Add")
       (page-title) => "Add friend"
       (fill-in firstname-field firstname)
       (fill-in lastname-field lastname)
       (fill-in notes-field notes)
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
                   (first-value firstname-field) => (u/string-of-length 100)
                   (first-value lastname-field) => "Yoda"
                   (first-value notes-field) => "Some notes"
                   (is-checked? "once a week") => true))
       (add-friend "Boba" "Fett" "Bounty Hunter" "once a week")
       (add-friend "Darth" "Vadar" "Breathy" "once a month")
       (check-friend-row 0 "Boba Fett" "once a week")
       (check-friend-row 1 "Darth Vadar" "once a month")
       (check-friend-details "Boba" "Fett" "Bounty Hunter" "once a week")
       (check-friend-details "Darth" "Vadar" "Breathy" "once a month"))

(facts "About when you've just seen a friend"
       (start-session (test-app (tc/to-long (t/date-time 2014 03 25))))
       (login-to-app)
       (add-friend "Anakin" "Skywalker" "a kid" "once a week")
       (follow "Friends")
       (page-title) => "My friends"
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
                    (follow "Friends") => nil
                    (follow "Add") => nil
                    (login-to-app)
                    (follow "Friends") =not=> nil
                    (follow "Add") =not=> nil)))
