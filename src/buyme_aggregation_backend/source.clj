(ns buyme-aggregation-backend.source
  (:require [buyme-aggregation-backend.db :as db]
            [buyme-aggregation-backend.sources.index :refer [source-impls]]
            [buyme-aggregation-backend.types :refer [fetch]]
            [buyme-aggregation-backend.helpers.lambda :refer [invoke-lambda-fn]]

            [clojure.core.async :refer [thread close! pipeline-async <!! >!! chan sliding-buffer]]
            [clojure.algo.generic.functor :refer [fmap]]
            [chime :refer [chime-ch]]
            [clj-time.core :refer [now plus minus hours minutes]]
            [clj-time.coerce :refer [to-long]]
            [clj-time.periodic :refer [periodic-seq]]
            [mount.core :refer [defstate]]
            [taoensso.timbre :refer [info]]))

(defn get-error-action [error]
  (let [reason (:reason (ex-data error))]
    (condp = reason
      :bad-request [:ignore]
      :not-found [:ignore]
      :server-error [:ignore]
      :bad-auth [:stop-source]
      :forbidden [:stop-source]
      :rate-limited [:block-until (-> (now)
                                      (plus (-> 3 hours))
                                      to-long)]
      [:ignore])))

;; based on ideas from https://github.com/Day8/re-frame/blob/master/src/re_frame/router.cljc
(defprotocol ISourceMachine
  ;(run [this])                                              ;; start looping
  (-next [this action data?])                               ;; next state given an action and maybe data
  (-enter [this state data?])                               ;; fn to run on entering state
  (send! [this action])                                     ;; send in event from outside
  ;(shutdown [this])                                         ;; interrupt/immediate shutdown source
  (get-state [this]))                                       ;; get info about current state

(deftype SourceMachine [^:volatile-mutable fsm-state
                        source]
  ISourceMachine
  (-next [this action data]
    (case [fsm-state action]
      ;; shutdown cleans up before moving to stopped state
      [:shutdown :stop] [:stopped]

      ;; stopped sources do not fetch based on timer events
      ; fixme
      [:stopped :start] [:idle]
      [:stopped :fetch] [:fetching]

      ;; idling sources are responsive to timer events
      [:idle :stop] [:shutdown]
      [:idle :fetch] [:fetching]

      [:fetching :done] [:idle]
      [:fetching :cease] [:idle]
      [:fetching :stop] [:shutdown]
      [:fetching :block-until] [:shutdown data]
      nil))

  (-enter [this state state-data]
    (case state
      :shutdown (do
                  (println "shutting down with" state-data)
                  (send! this :stop))

      :stopped (println "entered stopped state")

      :idle (println "entered idle state")

      :fetching (let [[work-ch source-command-ch] (fetch source nil)
                      consumer-result-ch (chan)]
                  (pipeline-async 75
                                  consumer-result-ch
                                  (fn [[status data] ch]
                                    (thread
                                      (case status
                                        :ok (let [result (invoke-lambda-fn
                                                           "fetch-store-image"
                                                           {:image-url   (str "https://i.imgur.com/" (:image_id data) ".png")
                                                            :bucket-name "buyme-aggregation-backend"
                                                            :file-name   (str "test/" (or (:title data) (:image_id data)) ".png")
                                                            :image-meta  (into {} data)})]

                                              (println (:image_id data))
                                              (println "uploaded work image" result)
                                              (>!! ch [:ok (:image_id data)]))

                                        :error (>!! ch [:error data]))
                                      (close! ch)))
                                  work-ch)

                  (loop [[status data] (<!! consumer-result-ch)]
                    (case status
                      ;; work unit completed
                      :ok
                      (recur (<!! consumer-result-ch))

                      ;; processing done
                      nil
                      (if (= state-data :once) (send! this :stop)
                                               (send! this :done))

                      ;; handle pipeline errors
                      :error
                      (let [error-action (get-error-action data)
                            command      (first error-action)
                            data         (second error-action)]
                        ;; stop api fetching if needed
                        (when (some #{command} [:cease :stop-source :block-until])
                          (>!! source-command-ch :stop))

                        (send! this (condp = command
                                      :cease :cease
                                      :stop-source :stop
                                      :block-until [:stop data]
                                      :stop))))))

      nil))

  (send! [this action]
    (let [[action data] (if (vector? action)
                          [(first action) (second action)]
                          [action nil])]
      ;; todo: figure out data-- could be passed into send from outside, also
      ;; from -next
      (when-let [[next-fsm-state next-fsm-state-data] (-next this action data)]
        (set! fsm-state next-fsm-state)
        (-enter this fsm-state data)))
    this)

  (get-state [this] fsm-state))


(defn init-source [source]
  (->SourceMachine :stopped source))

(defn start-source! [source]
  (send! source :star)
  source)

(defn stop-source [source]
  (send! source :stop))

(defn stop-all-sources [sources]
  (println sources)
  (fmap stop-source sources))

(defstate sources
          :start (->> (db/get-all-sources)
                      (map (fn [source-settings]
                             (when-let [impl (get source-impls (keyword (:source_impl_id source-settings)))]
                               (vector (:id source-settings)
                                       (init-source (impl source-settings))))))
                      (into {})
                      (fmap start-source!))
          :stop (do
                  (stop-all-sources sources)
                  {}))