(ns dunbar.test.controller
  (:require [midje.sweet :refer :all]
            [dunbar.mongo :refer [save! query]]
            [dunbar.controller :as c]
            [dunbar.test.test-components :refer [new-test-db new-test-clock]]
            [dunbar.clock :refer [date-time-millis]]
            [dunbar.test.helpers.builders :refer [build-friend]]))

(defn logged-in-request [username params]
  {:session {:user {:name username}}
   :params params})

(defn has-status? [status]
  (fn [response] (= (:status response) status)))

(defn has-redirect-location? [location]
  (fn [response] (= (get-in response [:headers "Location"]) location)))

(defn body-contains? [text]
  (fn [response]
    (re-find (re-pattern text) (:body response))))

(facts "Adding a friend"
       (fact "Can successfully add a friend"
             (let [db (new-test-db)
                   clock (new-test-clock 76)
                   request (logged-in-request "John" {:firstname "Joe" :lastname "Bloggs"
                                                      :notes "Some notes on Joe"
                                                      :meet-freq "28"})]
               (c/add-friend db clock request) => (every-checker
                                             (has-status? 302)
                                             (has-redirect-location? "/friends"))
               (query db "friends" {}) => [{:user "John" :firstname "Joe" :lastname "Bloggs"
                                            :notes "Some notes on Joe"
                                            :meet-freq 28
                                            :id "joe-bloggs" :created-at 76}]))
       (fact "Friend not added if invalid"
             (let [db (new-test-db)
                   clock (new-test-clock 0)
                   request (logged-in-request "John" {})]
               (c/add-friend db clock request) => (has-status? 200)
               (query db "friends" {}) => [])))

(facts "Friend list"
       (fact "Friends in database are shown on page"
             (let [db (new-test-db)
                   clock (new-test-clock 0)
                   request (logged-in-request "John" {})]
                   (save! db "friends" (build-friend {:user "John" :firstname "Jimi"  :lastname "Hendrix"}))
                   (save! db "friends" (build-friend {:user "John" :firstname "Jimmy" :lastname "Page"}))
                   (save! db "friends" (build-friend {:user "Jack" :firstname "Jack"  :lastname "White"}))
                   (c/friend-list db clock request) => (every-checker
                                                  (has-status? 200)
                                                  (body-contains? "Jimi Hendrix")
                                                  (body-contains? "Jimmy Page")
                                                  (complement (body-contains? "Jack White"))))))

(facts "Friend list update"
       (fact "posting id sets last seen for friend"
             (let [db (new-test-db)
                   clock (new-test-clock 234)
                   request (logged-in-request "user" {:just-seen "id"})]
               (save! db "friends" {:user "user" :firstname "Bob" :lastname "Hoskins" :id "id"})
               (c/friend-list-update db clock request) => (has-redirect-location? "/friends")
               (first (query db "friends" {:id "id" :user "user"})) => (contains {:last-seen 234}))))

(facts "About secured routes"
       (let [handlers (c/handlers (new-test-db) (new-test-clock 0) {} {})]
         (fact "Must be logged in to view add-friend-form"
               ((:add-friend-form handlers) (logged-in-request "J" {})) =not=> (has-redirect-location? "/login")
               ((:add-friend-form handlers) {}) => (has-redirect-location? "/login"))
         (fact "Must be logged in to add friend"
               ((:add-friend handlers) (logged-in-request "J" {})) =not=> (has-redirect-location? "/login")
               ((:add-friend handlers) {}) => (has-redirect-location? "/login"))
         (fact "Must be logged in to view friend list"
               ((:friend-list handlers) (logged-in-request "J" {})) =not=> (has-redirect-location? "/login")
               ((:friend-list handlers) {}) => (has-redirect-location? "/login"))
         (fact "Must be logged in to view friend details page"
               ((:friend-list handlers) (logged-in-request "J" {})) =not=> (has-redirect-location? "/login")
               ((:friend-list handlers) {}) => (has-redirect-location? "/login"))
         ))
