(ns buyme-aggregation-backend.util.misc)

(defn key-by
  "given a seq of maps, return a map whos keys are the value of the
  specified key in each item"
  [key seq]
  (into {} (map #(vector (get % key) %) seq)))
