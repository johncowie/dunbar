(ns dunbar.test.functional
  (:require [midje.sweet :refer :all]
            [kerodon.core :as k]
            [dunbar.handler :refer [make-app]]
            [dunbar.test.test-components :refer [new-test-db]]
            [dunbar.test.test-utils :as u]
            [net.cgrand.enlive-html :as html]
            ))

(def state (atom {}))

(defn start-session [app]
  (swap! state (constantly (k/session app))))

(defn end-session []
  (swap! state (constantly {})))

(defn follow-redirect []
  (swap! state k/follow-redirect))

(defn auto-follow-redirect []
  (let [status (-> @state :response :status)
        location (-> @state (get-in [:response :headers "Location"]))]
    (when (and location (not (= status 200)))
      (do (follow-redirect) (auto-follow-redirect)))))

(defn visit [path]
  (swap! state #(k/visit % path))
  (auto-follow-redirect))

(defn fill-in [selector value]
  (swap! state #(k/fill-in % selector value)))

(defn press [selector]
  (swap! state #(k/press % selector))
  (auto-follow-redirect))

(defn follow [selector]
  (swap! state #(k/follow % selector)))

(defn text [selector]
  (map (comp first :content)
       (-> @state :enlive (html/select selector))))

(defn first-text [selector]
  (first (text selector)))

(defn value [selector]
  (map (comp :value :attrs)
       (-> @state :enlive (html/select selector))))

(defn first-value [selector]
  (first (value selector)))

(defn page-title []
  (first (text [:title])))

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
       (first (text [:td.friend-name])) => "Boba Fett"
       (first (text [:td.friend-notes])) => "Bounty Hunter"
       (second (text [:td.friend-name])) => "Darth Vadar"
       (second (text [:td.friend-notes])) => "Breathy")
