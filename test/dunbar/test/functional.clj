(ns dunbar.test.functional
  (:require [midje.sweet :refer :all]
            [kerodon.stateful :refer :all]
            [dunbar.handler :refer [make-app]]
            [dunbar.clock :refer [date-time-millis]]
            [dunbar.components.stubs :refer [new-test-db new-test-clock adjust]]
            [dunbar.test.test-utils :as u]
            [dunbar.test.helpers.builders :as b]
            [dunbar.oauth.twitter :refer [new-stub-twitter-oauth]]
            [net.cgrand.enlive-html :as html]
            [clj-time.coerce :as tc]
            [clj-time.core :as t]))

(defn test-app
  ([clock] (make-app (new-test-db) clock (new-stub-twitter-oauth (b/build-twitter-user {:name "Geoff"}))))
  ([] (test-app (new-test-clock 0))))

(defn login-to-app []
  (facts "Can login to app"
         (visit "/")
         (press "Sign in with Twitter")
         (page-title) => "My friends"))

(defn add-friend [firstname lastname notes meet-freq]
  (facts (str "Adding a friend with name " firstname " " lastname)
       (follow "Add")
       (page-title) => "Add friend"
       (fill-in "First name:" firstname)
       (fill-in "Last name:" lastname)
       (fill-in "Notes:" notes)
       (check meet-freq)
       (press "Add Friend")))

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
         (text [:#friend-details-name]) => (str firstname " " lastname)
         (text [:#friend-details-meet-freq]) => meet-freq
         (text [:#friend-details-notes]) => notes))

(future-fact "Login redirects to previously requested page (if valid page)"
       (start-session (test-app))
       (visit "/friends/bob")
       (page-title) => "Login"
       (press "Sign in with Twitter")
       (current-url) => "/friends/bob"
       (page-title) => "Nothing to see here..")

(facts "Logout clears session and user can no longer see friends page"
       (start-session (test-app))
       (fact "Can't initially view secure page"
             (visit "/friends")
             (page-title) => "Login")
       (login-to-app)
       (fact "After login can view secured page"
             (visit "/friends")
             (page-title) => "My friends")
       (fact "When logging out am taken to Login page"
             (follow "Logout, Geoff")
             (page-title) => "Login")
       (fact "When returning to secured page, taken back to login"
             (visit "/friends")
             (page-title) => "Login"))

(facts "Creating a friend"
       (start-session (test-app))
       (login-to-app)
       (fact "Creating an invalid friend returns validation error"
             (add-friend (u/string-of-length 100) "Yoda" "Some notes" "once a week")
             (page-title) => "Add friend"
             (text [:.validation-errors :li]) =not=> empty?
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
         (follow "Friends")
         (add-friend "Anakin" "Skywalker" "a kid" "once a week")
         (follow "Friends")
         (text [:.friend-overdue-seen]) => "-7"
         (follow "Anakin Skywalker")
         (text [:#friend-details-last-seen]) => "-"
         (follow "Friends")
         (press "Just seen them")
         (follow "Anakin Skywalker")
         (text [:#friend-details-last-seen]) => "25 MAR 2014"
         (adjust clock (date-time-millis 2014 4 3))
         (follow "Friends")
         (text [:.friend-overdue-seen]) => "2"
         (follow "Anakin Skywalker")
         (text [:#friend-details-overdue-seen]) => "2"))

(facts "Zero state message when no friends have been added"
       (start-session (test-app))
       (login-to-app)
       (follow "Friends")
       (count (elements [:.zero-state])) => 1
       (count (elements [:.table])) => 0
       (follow [:.add-friend-link])
       (page-title) => "Add friend"
       (add-friend "Darth" "Maul" "he's bad" "once a week")
       (follow "Friends")
       (elements [:.zero-state]) => [])

(facts "General hygiene stuff"
       (start-session (test-app))
       (fact "can get 404 page for unknown url"
             (login-to-app)
             (visit "/blah")
             (page-title) => "Nothing to see here.."
             (status) => 404)
       (facts "navigation"
              (fact "can only see navigation if logged in"
                    (start-session (test-app))
                    (follow "Friends") => (throws Exception)
                    (follow "Add") => (throws Exception)
                    (login-to-app)
                    (follow "Friends")
                    (page-title) => "My friends"
                    (follow "Add")
                    (page-title) => "Add friend"
                    )))
