(ns buyme-aggregation-backend.sources.imgur
  (:require [buyme-aggregation-backend.types :refer [source]]
            [buyme-aggregation-backend.conf :refer [config]]
            [clj-http.client :as client]
            [cemerick.url :refer [url]]))

(def api-root "https://api.imgur.com/3")
(def tag-gallery-path "gallery/t")
;(def album-path "gallery/t")
(def imgur-http-config {:headers
                        {:authorization (str "Client-ID " (config :imgur-client-id))
                         :accept :json}
                        :as :json})

(defn fetch-tag-items [tag]
  (let [url (str (url api-root tag-gallery-path tag))]
    (get-in (client/get url imgur-http-config) [:body :data])))



; look at item.is_album
; if so, fetch the album -> data.images[]
(defn make-source [settings]
  (reify source
    (fetch [_] (println (fetch-tag-items "wallpaper")))
    (parse [_ data] (print "parse imgur") (str (:test data) "parsed"))))