(ns buyme-aggregation-backend.util.misc
  (:require [clojure.core.async :refer [chan thread <!!]]))


(defn key-by
  "given a seq of maps, return a map whos keys are the value of the
  specified key in each item"
  [key seq]
  (into {} (map #(vector (get % key) %) seq)))

(defn with-thread-pool [n work-ch handle]
  (doseq [_ n]
     (thread
       (loop []
         (let [w (<!! work-ch)]
           (try
             (handle w)
             (catch Throwable t
               (println "fixme" t))))
         (recur)))))


