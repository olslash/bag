(ns buyme-aggregation-backend.source
  (:require [buyme-aggregation-backend.db :as db]
            [buyme-aggregation-backend.sources.index :refer [source-impls]]
            [buyme-aggregation-backend.types :refer [fetch parse]]

            [clojure.core.async :refer [chan go-loop <! alt! sliding-buffer put! close!]]
            [clojure.algo.generic.functor :refer [fmap]]
            [chime :refer [chime-ch]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]]
            [mount.core :refer [defstate]]
            [taoensso.timbre :refer [info]]))


(defn init-source [source]
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

(defn start-source [ch]
  (put! ch :start)
  ch)

(defn stop-source [ch]
  (put! ch :stop)
  (close! ch))

(defn stop-all-sources [sources]
  (fmap stop-source sources))


(defn source-impl-id [settings]
  (keyword (:source_impl_id settings)))

(defstate sources
          :start (->> (db/get-all-sources)
                      (map (fn [source-settings]
                             (let [impl (get source-impls (source-impl-id source-settings))]
                               (when impl
                                 (vector (:id source-settings) (init-source (impl source-settings)))))))

                      (into {})
                      (fmap start-source))
          :stop (do
                  (stop-all-sources sources)
                  {}))



