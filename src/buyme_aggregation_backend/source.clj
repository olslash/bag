(ns buyme-aggregation-backend.source
  (:require [buyme-aggregation-backend.db :as db]
            [buyme-aggregation-backend.sources.index :refer [source-impls]]
            [buyme-aggregation-backend.types :refer [fetch parse]]

            [clojure.core.async :refer [chan go-loop <! alt! sliding-buffer]]
            [chime :refer [chime-ch]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [mount.core :refer [defstate]]
            [taoensso.timbre :refer [info]]))


(defn start-source [source]
  (let [command-ch (chan)
        fetch-timer-ch (chime-ch (rest (periodic-seq (t/now) (-> 5 t/seconds)))
                                 {:ch (chan (sliding-buffer 1))})]
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
                           :stopped))
              :running (alt!
                         command-ch ([command] (condp = command
                                                 :stop :shutdown
                                                 :running))
                         fetch-timer-ch (do
                                          (info "got a chime")
                                          (let [data (fetch source)]
                                            (parse source data))
                                          :running)))]
        (recur new-state)))
    command-ch))

(defn stop-all-sources []
  (println "stopping"))

(defstate sources
          :start (->> (db/get-all-sources)
                      ; fixme -- more descriptive IDs and don't use name
                      (map #(vector (:name %)
                                    (start-source ((get source-impls (:name %)) %))))
                      (into {}))
          :stop (stop-all-sources))
