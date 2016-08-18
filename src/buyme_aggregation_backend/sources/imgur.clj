(ns buyme-aggregation-backend.sources.imgur
  (:require [buyme-aggregation-backend.types :refer [source map->image]]
            [buyme-aggregation-backend.conf :refer [config]]
            [buyme-aggregation-backend.util.misc :refer [key-by]]
            [clojure.algo.generic.functor :refer [fmap]]
            [clojure.set :refer [rename-keys]]
            [org.httpkit.client :as http]
            [cheshire.core :as json]
            [cemerick.url :refer [url]]))

(def api-root "https://api.imgur.com/3")
(def tag-gallery-path "gallery/t")
(def album-path "album")
(def imgur-http-config {:headers
                        {"Authorization" (str "Client-ID " (config :imgur-client-id))
                         "Accept" "application/json"}})

(defn parse-response-body [res]
  (-> res
      (get :body)
      (json/parse-string true)
      (get :data)))

(defn fetch-tag-items [tag]
  (let [tag-gallery-url (str (url api-root tag-gallery-path tag))]
    (-> @(http/get tag-gallery-url imgur-http-config)
        (parse-response-body)
        (get :items))))

; fetch--
; fetch-tag-items: [image1, album1, image2, image3, album 2]
; fetch each album, key by id
;
; parse--
; for each album item, merge the album's props onto it -- uploader id, nsfw, description ("alb desc / image desc")
; flatmap over tag items, if is_album, replace album with its associated images
; api-image->image

(defn album-url [id]
  (str (url api-root album-path id)))

(defn attach-album-meta-to-images
  "merge album-level metadata into each image in that album"
  [album]
  ; todo -- merge-with and combine title/descriptions
  (assoc album :images (map #(merge (dissoc album :images) %)
                             (:images album))))


(defn api-image->image [api-image]
  (let [translate {:account_id :uploader-id}]
     (map->image (rename-keys api-image translate))))




(defn make-source [settings]
  (reify source
    (fetch [_] (let [tag-items (fetch-tag-items "wallpaper")
                     album-urls (->> tag-items
                                     (map #(when (:is_album %) (album-url (:id %))))
                                     (filter some?))
                     fetch-album-promises (doall (map #(http/get % imgur-http-config) album-urls))]
                 {:tag-items tag-items
                  :albums (->> fetch-album-promises
                               (map #(-> % (deref) (parse-response-body)))
                               (key-by :id))}))

    (parse [_ {:keys [tag-items albums]}]
      (let [fat-album-images (->> albums
                                  (fmap attach-album-meta-to-images)
                                  (fmap :images))]
        (->> tag-items
             (map #(if (:is_album %) (get fat-album-images (:id %)) %))
             (flatten)
             (map api-image->image))))))


