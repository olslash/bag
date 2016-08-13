(ns buyme-aggregation-backend.types)

(defprotocol source
  (fetch [this])
  (parse [this data]))
