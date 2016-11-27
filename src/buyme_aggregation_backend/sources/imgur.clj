(ns buyme-aggregation-backend.sources.imgur
  (:require [buyme-aggregation-backend.types :refer [Source make-image]]
            [buyme-aggregation-backend.conf :refer [config]]
            [buyme-aggregation-backend.util.misc :refer [key-by]]
            [buyme-aggregation-backend.util.async :refer [with-thread-pool]]
            [buyme-aggregation-backend.helpers.lambda :refer [invoke-lambda-fn]]
            [clojure.core.async :refer [chan go >! <! pipeline-async to-chan close! timeout onto-chan]]
            [clojure.algo.generic.functor :refer [fmap]]
            [clojure.set :refer [rename-keys]]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [cemerick.url :refer [url]]))

;; todo: get these from config passed to fetch/etc
(def api-root "https://api.imgur.com/3")
(def tag-gallery-path "gallery/t")
(def album-path "album")
(def imgur-http-config {:headers
                        {"Authorization" (str "Client-ID " (config :imgur-client-id))
                         "Accept"        "application/json"}})
(def parallelism 2)

(defn api-image->Image [api-image]
  (let [translate {:account_id  :attribution_id
                   :account_url :attribution_name
                   :id          :image_id}]
    (-> api-image
        (rename-keys translate)
        (make-image))))


(defn album-url [id]
  (str (url api-root album-path id)))

(defn parse-response-body [res]
  (-> res
      (:body)
      (json/parse-string true)
      (:data)))

(defn fetch-tag-items [tag]
  (let [tag-gallery-url (str (url api-root tag-gallery-path tag))]
    (http/get tag-gallery-url imgur-http-config)))

(defn fetch-album [id]
  (let [url (album-url id)]
    (http/get url imgur-http-config)))

(defn attach-album-meta-to-images
  "merge album-level metadata into each image in that album"
  [album]
  ; todo -- merge-with and combine title/descriptions
  (assoc album :images (map #(merge (dissoc album :images) %)
                            (:images album))))


(defn make-source [_]
  (reify Source
    (fetch [_ config]
      (let [result-ch (chan)
            tag-items-promise (fetch-tag-items "wallpaper")]
        (go
          (let [tag-items (-> @tag-items-promise
                              (parse-response-body)
                              :items)
                images-albums (group-by :is_album tag-items)
                tag-albums (get images-albums true)
                tag-images (get images-albums false)]

            ;; put free images
            (doseq [image tag-images]
              (->> image
                   (api-image->Image)
                   (>! result-ch)))

            ;; fetch albums and put their images
            (letfn [(process-album [album-meta out-ch]
                      (-> @(fetch-album (:id album-meta))
                          (parse-response-body)
                          #_(attach-album-meta-to-images)
                          :images
                          (#(map api-image->Image %))
                          (#(onto-chan out-ch %))))]

              (pipeline-async parallelism
                              result-ch
                              process-album
                              (to-chan tag-albums)))))

        result-ch))))
