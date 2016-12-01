(ns buyme-aggregation-backend.source
  (:require [buyme-aggregation-backend.db :as db]
            [buyme-aggregation-backend.sources.index :refer [source-impls]]
            [buyme-aggregation-backend.types :refer [fetch]]
            [buyme-aggregation-backend.util.async :refer [blocking-consumer]]

            [clojure.core.async :refer [chan go-loop <! alt! sliding-buffer put! close! thread]]
            [clojure.algo.generic.functor :refer [fmap]]
            [clojure.core.match :refer [match]]
            [chime :refer [chime-ch]]
            [clj-time.core :refer [now plus minus hours minutes]]
            [clj-time.coerce :refer [to-long]]
            [clj-time.periodic :refer [periodic-seq]]
            [mount.core :refer [defstate]]
            [taoensso.timbre :refer [info]]))

(defn get-error-action [reason]
  "non-200, "
  (condp = reason
    :bad-request [:ignore]
    :bad-auth [:cease :stop-source]
    :forbidden [:cease :stop-source]
    :not-found [:ignore]
    :server-error [:ignore]
    :rate-limited [:case :stop-source [:block-until (-> (now)
                                                        (plus (-> 3 hours))
                                                        to-long)]]))

(defn init-source [source]
  (let [command-ch (chan)
        fetch-timer-ch (chime-ch (periodic-seq (now) (-> 10 minutes))
                                 {:ch (chan (sliding-buffer 1))})]
    (go-loop
      [[state data] [:stopped]]
      (let [new-state
            (case state
              :shutdown (do
                          (print "shutting down...")
                          [:stopped])

              :stopped (let [command (<! command-ch)]
                         (condp = command
                           :start [:idle]
                           :fetch [:fetching :once]
                           [:stopped]))

              :idle (alt!
                      command-ch ([command] (condp = command
                                              :stop [:shutdown]
                                              :fetch [:fetching]
                                              [:idle]))
                      fetch-timer-ch [:fetching])

              :fetching (do
                          ;; todo:
                          ;; pull config from db and inject into (fetch)
                          ;; s3 store w/ lambda + meta?
                          ;; save meta to DB?
                          (let [work-ch (fetch source nil)]
                            (blocking-consumer 3
                                               work-ch
                                               (fn [[status data]]
                                                 (match [status data]
                                                        [:ok image] (println "got work image" image)
                                                        [:error message] (println "ERROR:" message)))))
                          (if (= data :once) [:stopped]
                                             [:idle])))]

        (recur new-state)))
    command-ch))

(defn start-source [ch]
  (put! ch :start)
  ch)

(defn stop-source [ch]
  (put! ch :stop (constantly #(close! ch))))

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



