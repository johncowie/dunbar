(ns dunbar.test.controller
  (:require [midje.sweet :refer :all]
            [dunbar.mongo :refer [save! query]]
            [dunbar.controller :as c]
            [dunbar.test.test-components :refer [new-test-db]]))

(defn logged-in-request [username params]
  {:session {:username username}
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
                   request (logged-in-request "John" {:firstname "Joe" :lastname "Bloggs"
                                                      :notes "Some notes on Joe"})]
               (c/add-friend db request) => (every-checker
                                             (has-status? 302)
                                             (has-redirect-location? "/friends"))
               (query db "friends" {}) => [{:user "John" :firstname "Joe" :lastname "Bloggs"
                                            :notes "Some notes on Joe"}])))

(facts "Friend list"
       (fact "Friends in database are shown on page"
             (let [db (new-test-db)
                   request (logged-in-request "John" {})]
                   (save! db "friends" {:user "John" :firstname "Jimi" :lastname "Hendrix"})
                   (save! db "friends" {:user "John" :firstname "Jimmy" :lastname "Page"})
                   (save! db "friends" {:user "Jack" :firstname "Jack" :lastname "White"})
                   (c/friend-list db request) => (every-checker
                                                  (has-status? 200)
                                                  (body-contains? "Jimi Hendrix")
                                                  (body-contains? "Jimmy Page")
                                                  (complement (body-contains? "Jack White"))))))

(facts "About secured routes"
       (let [handlers (c/handlers (new-test-db))]
         (fact "Must be logged in to view add-friend-form"
               ((:add-friend-form handlers) (logged-in-request "J" {})) =not=> (has-redirect-location? "/login")
               ((:add-friend-form handlers) {}) => (has-redirect-location? "/login"))
         (fact "Must be logged in to add friend"
               ((:add-friend handlers) (logged-in-request "J" {})) =not=> (has-redirect-location? "/login")
               ((:add-friend handlers) {}) => (has-redirect-location? "/login"))
         (fact "Must be logged in to view friend list"
               ((:friend-list handlers) (logged-in-request "J" {})) =not=> (has-redirect-location? "/login")
               ((:friend-list handlers) {}) => (has-redirect-location? "/login"))))
