(ns dunbar.oauth.twitter
  (:require [cheshire.core :as json]
            [com.stuartsierra.component :as component])
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

(defn instance [this]
  (.getInstance (:twitter-factory this)))

(defrecord Twitter4JOAuth [consumer-key consumer-secret]
  component/Lifecycle
  (start [this]
    (prn "Starting twitter..")
    (assoc this :twitter-factory (TwitterFactory. (twitter-config consumer-key consumer-secret))))
  (stop [this]
    (prn "Stopping twitter..")
    (dissoc this :twitter-factory))
  TwitterOAuth
  (get-request-token [this callback-url]
    (let [twitter-instance (instance this)
          request-token (. twitter-instance (getOAuthRequestToken callback-url))]
          {:request-token {:token (.getToken request-token)
                           :token-secret (.getTokenSecret request-token)}
                           :authentication-url (.getAuthenticationURL request-token)}))
  (callback [this request-token oauth-verifier]
      (when (and request-token oauth-verifier)
        (let [twitter-instance (instance this)]
          (. twitter-instance (getOAuthAccessToken (new RequestToken (:token request-token) (:token-secret request-token)) oauth-verifier))
          (let [user (. twitter-instance (showUser (. twitter-instance (getId))))]
            (-> (TwitterObjectFactory/getRawJSON user) (json/parse-string keyword)))))))

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
