(ns buyme-aggregation-backend.types)

(defprotocol source
  (fetch [this])
  (parse [this data]))

(defrecord image [id title description width height uploader-id nsfw])