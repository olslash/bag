(ns buyme-aggregation-backend.sources
  (:require [buyme-aggregation-backend.db :as db]
            [clojure.core.async :refer [chan go-loop <! alt!]]
            [chime :refer [chime-ch]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [mount.core :refer [defstate]]
            [taoensso.timbre :refer [info]]))

(def sources (atom {}))

(defn get-source-settings []
  (db/get-all-sources))

(defn start-source []
  (let [command-ch (chan)
        fetch-timer-ch (chime-ch (rest (periodic-seq (t/now) (-> 5 t/seconds))))]
    (go-loop
      [state :stopped]
      (let [new-state
            (case state
              :shutdown (do
                          (print "shutting down...")
                          :stopped)
              :stopped (let [command (<! command-ch)]
                         (condp = command
                           :start :running
                           :default :stopped))
              :running (alt!
                         command-ch ([command] (condp = command
                                                 :stop :shutdown
                                                 :default :running))
                         fetch-timer-ch (do
                                          (println "got a chime")
                                          :running)))]
        (recur new-state)))
    command-ch))

(defn stop-all-sources []
  (println "stopping"))

(defstate sources
          :start (->> (get-source-settings) (map start-source))
          :stop (stop-all-sources))

