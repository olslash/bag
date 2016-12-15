(ns buyme-aggregation-backend.types)

(defprotocol Source
  ; fetch :: (source, config) => chan;
  ; put onto result chan Image records. must close! when done
  (fetch [this config])
  ; upload :: (source, input-chan) => chan;
  ; take from input-ch partial image records (won't have s3_url)
  ; get the image onto s3, populate remaining record fields, put onto result chan.
  ; must close! when done
  ; todo: this is generic, put in source machine
  #_(upload [this input-ch])
  ; store :: (source, input-chan) => void;
  ; take from input-ch completed image records that need to be stored in the DB.
  ; store them, returning when the input-chan closes.
  ; todo: this is generic, put in source machine
  #_(save-meta [this input-ch]))



;; mirrors images db table
(defrecord Image [source_id                                 ; like "imgur"
                  image_id
                  s3_url
                  slug
                  title
                  description
                  attribution_id
                  attribution_name
                  attribution_url
                  width
                  height
                  type
                  nsfw])

(defn make-image [image-map]
  (-> image-map
      (select-keys (map keyword (Image/getBasis)))
      (map->Image)))
