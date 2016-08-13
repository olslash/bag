(ns buyme-aggregation-backend.sources.imgur
  (:require [buyme-aggregation-backend.types :refer [source]]))

(defn make-source [settings]
  (reify source
    (fetch [_] (print "fetch imgur  " settings) {:test "data"})
    (parse [_ data] (print "parse imgur") (str (:test data) "parsed"))))