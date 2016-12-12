(ns buyme-aggregation-backend.util.async
  (:require [clojure.core.async :as a :refer [chan thread <!! poll!]]))


(defn with-thread-pool [n work-ch handle]
  (doseq [_ n]
    (thread
      (loop []
        (let [w (<!! work-ch)]
          (try
            (handle w)
            (catch Throwable t)))
        ;(println "fixme" t))))
        (recur)))))



;; https://gist.github.com/favila/8e7ad6ea5b01bd7466ff
(defn blocking-consumer
  "Spawn `n` threads, each of which will read from channel `ch` and call
  `f` with the read value as its only argument. Presumably, `f` will case a
  side-effect with the value.
  If `f` does something long-running, it should block to exert back-pressure
  on ch.
  Returns a channel which closes when all workers close. All workers will close
  when `ch` closes."
  [n ch f]
  (letfn [(make-worker []
            (thread
              (loop [v (<!! ch)]
                (when-not (nil? v)
                  (f v)
                  (recur (<!! ch))))))]
    (a/merge (repeatedly n make-worker))))
