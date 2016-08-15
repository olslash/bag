(ns buyme-aggregation-backend.utils.logging
  (:require [clj-http.client]
            [robert.hooke :refer [add-hook clear-hooks]]
            [taoensso.timbre :as timbre :refer [info]]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]))

(defn do-logging-config [config]
  (info "configuring logging")
  (timbre/set-level! (keyword (config :log-level)))
  (timbre/merge-config!
    {:appenders
     {:rotor (rotor/rotor-appender {:path "./buyme_aggregation_backend.log"
                                    :min-level  :debug})}}))


(defn log-http-get [f & args]
  (info "HTTP Fetch:" args)
  (apply f args))

(clear-hooks #'clj-http.client/get)
(add-hook #'clj-http.client/get #'log-http-get)