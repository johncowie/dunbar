(ns dunbar.oauth.twitter
  (:require [cheshire.core :as json])
  (:import  [twitter4j Twitter TwitterFactory TwitterObjectFactory]
            [twitter4j.conf PropertyConfiguration ConfigurationBuilder]
            [twitter4j.auth RequestToken]))

(defn twitter-config [consumer-key consumer-secret]
   (->
     (new ConfigurationBuilder)
     (.setJSONStoreEnabled true)
     (.setOAuthConsumerKey consumer-key)
     (.setOAuthConsumerSecret consumer-secret)
     (.build)))

(defprotocol TwitterOAuth
  (get-request-token [this callback-url])
  (callback [this request-token oauth-verifier]))

(defrecord Twitter4JOAuth [consumer-key consumer-secret]
  TwitterOAuth
  (get-request-token [this callback-url]
    (let [twitter (. (TwitterFactory. (twitter-config consumer-key consumer-secret)) (getInstance))
          request-token (. twitter (getOAuthRequestToken callback-url))]
          {:request-token {:token (.getToken request-token)
                           :token-secret (.getTokenSecret request-token)}
                           :authentication-url (.getAuthenticationURL request-token)}))
  (callback [this requestToken oauth-verifier]
    (let [twitter (. (TwitterFactory. (twitter-config consumer-key consumer-secret)) (getInstance))]
      (. twitter (getOAuthAccessToken (new RequestToken (:token requestToken) (:token-secret requestToken)) oauth-verifier))
      (let [user (. twitter (showUser (. twitter (getId))))]
        (-> (TwitterObjectFactory/getRawJSON user) (json/parse-string keyword))))))

(defn new-twitter-oauth [consumer-key consumer-secret]
  (Twitter4JOAuth. consumer-key consumer-secret))

(defrecord StubTwitterOAuth [fake-user]
  TwitterOAuth
  (get-request-token [this callback-url]
                     {:request-token "blah" :authentication-url callback-url})
  (callback [this request-token oauth-verifier]
            fake-user))

(defn new-stub-twitter-oauth [fake-user]
  (StubTwitterOAuth. fake-user))
