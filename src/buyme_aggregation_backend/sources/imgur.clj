(ns buyme-aggregation-backend.sources.imgur
  (:require [buyme-aggregation-backend.types :refer [source]]
            [buyme-aggregation-backend.conf :refer [config]]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [cemerick.url :refer [url]]))

(def api-root "https://api.imgur.com/3")
(def tag-gallery-path "gallery/t")
;(def album-path "gallery/t")
(def imgur-http-config {:headers
                        {"Authorization" (str "Client-ID " (config :imgur-client-id))
                         "Accept" "application/json"}})


(defn fetch-tag-items [tag]
  (let [url (str (url api-root tag-gallery-path tag))]
    (-> (:body @(http/get url imgur-http-config))
        (json/parse-string true)
        (get-in [:data :items])
        (println))))



; look at item.is_album
; if so, fetch the album -> data.images[]
(defn make-source [settings]
  (reify source
    (fetch [_] (println (fetch-tag-items "wallpaper")))
    (parse [_ data] (print "parse imgur") (str (:test data) "parsed"))))