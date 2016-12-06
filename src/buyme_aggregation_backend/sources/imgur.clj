(ns buyme-aggregation-backend.sources.imgur
  (:require [buyme-aggregation-backend.types :refer [Source make-image]]
            [buyme-aggregation-backend.conf :refer [config]]
            [buyme-aggregation-backend.util.misc :refer [key-by]]
            [buyme-aggregation-backend.util.async :refer [with-thread-pool]]
            [buyme-aggregation-backend.helpers.lambda :refer [invoke-lambda-fn]]
            [clojure.core.async :refer [alt! go-loop chan thread >! <! pipeline-async to-chan close! timeout onto-chan]]
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
        make-image)))


(defn album-url [id]
  (str (url api-root album-path id)))

(defn parse-response-body [res]
  (update res :body json/parse-string true))



(def errors {400 :bad-request
             401 :bad-auth
             403 :forbidden
             404 :not-found
             420 :rate-limited
             500 :server-error})

;; todo: move to generic place
(defn fallback-error-for [code]
  (println "fallback" code)
  (cond
    (and (>= code 300)
         (< code 400)) :redirected
    (and (>= code 400)
         (< code 500)) :bad-request
    (>= code 500) :server-error
    :else nil))

(defn code->error [code]
  (let [defined-type (get errors code)]
    (or defined-type (fallback-error-for code))))

(defn fetch-tag-items [tag]
  (future
    (let [tag-gallery-url (str (url api-root tag-gallery-path tag))
          res @(http/get tag-gallery-url imgur-http-config)]
      (when-let [error (code->error (:status res))]
        (throw (ex-info
                 (str "Failed to fetch imgur tag items for:" tag)
                 {:reason error})))
      (-> res parse-response-body :body :data))))


(defn fetch-album [id]
  (future
    (let [album-url (album-url id)
          res @(http/get album-url imgur-http-config)]
      (when-let [error (code->error (:status res))]
        (throw (ex-info
                 (str "Failed to fetch imgur album:" id)
                 {:reason error})))
      (-> res parse-response-body :body :data))))

; fixme -- album images often have no title/description so we need album meta
(defn attach-album-meta-to-images
  "merge album-level metadata into each image in that album"
  [album]
  ; todo -- merge-with and combine title/descriptions
  (assoc album :images (map #(merge (dissoc album :images) %)
                            (:images album))))




(defrecord imgur-source [source-settings]
  Source
  (fetch [_ fetch-settings]
    (let [result-ch (chan 100)
          command-ch (chan)]
      (thread (try
                (let [images-albums (->> @(fetch-tag-items "wallpaper")
                                         :items
                                         (group-by #(if (:is_album %) :albums :images)))]
                  ;; put free images
                  (doseq [image (:images images-albums)]
                    (as-> image i
                          (api-image->Image i)
                          (>! result-ch [:ok i])))

                  ;; fetch albums and put their images
                  (letfn [(process-album [album-meta out-ch]
                            (try
                              (as-> @(fetch-album (:id album-meta)) $
                                    #_(attach-album-meta-to-images $)
                                    (:images $)
                                    (map api-image->Image $)
                                    (map #(identity [:ok %]) $)
                                    (onto-chan out-ch $))
                              (catch Exception e
                                (>! result-ch [:error e])
                                (close! out-ch))))]

                    (alt!
                      (pipeline-async parallelism
                                      result-ch
                                      process-album
                                      (to-chan (:albums images-albums)))
                      ([_])                                 ;; noop

                      command-ch ([command] (if (= :stop command)
                                              ;; stop pipeline
                                              (close! result-ch))))))

                (catch Exception e
                  (>! result-ch [:error e]))))

      [result-ch command-ch])))
