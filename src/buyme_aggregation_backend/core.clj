;; objectives:
;; maintain an editable list of image sources in DB
;;   feeds (atom, rss, ?)
;;   API LIST endpoints
;;
;; poll those feeds at a slow interval, aggregating images as they're posted
;;   in the case that there are multiple images in an album, grab them all
;; store images in DB
;;
;; expose a management API:
;;   add/remove feeds, change params on feed (ratelimit)
;;   manage existing images -- list, remove
;;   add images directly by url
;;   manually refresh a source
;;   start/stop auto-refresh of a source
;;
;; authenticate requests
;;   will recieve JWT with requests, and verify it with auth service
;;
;; /sources: GET    /          -- list all sources
;;           POST   /          -- add a source {url, name, type, ratelimit}
;;           PATCH  /:id       -- update a source (change url, rename)
;;           DELETE /:id       -- delete a source
;;           POST   /:id/stop  -- stop auto-fetching a source
;;           POST   /:id/start -- (re)start auto-fetching a source
;;           POST   /:id/fetch -- manually trigger a fetch from source
;;
;;
;; /images: GET    /      -- list all images (sort, pagination)
;;          POST   /      -- add an image {url}
;;          DELETE /:id   -- remove an image
;;


(ns buyme-aggregation-backend.core
  (:require [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [bidi.ring :refer [make-handler]]
            [environ.core :refer [env]]
            )
  (:gen-class))


(def sources-collection-handler
  (resource
    :available-media-types ["text/plain"]
    :handle-ok "this is the sources handler"))

(def source-handler
  (resource
    :available-media-types ["text/plain"]
    :handle-ok "this is the single source handler"))

(def source-action-handler
  (resource
    :available-media-types ["text/plain"]
    :handle-ok "this is the source action handler"))


(def images-collection-handler
  (resource
    :available-media-types ["text/plain"]
    :handle-ok "this is the images handler"))

(def image-handler
  (resource
    :available-media-types ["text/plain"]
    :handle-ok "this is the single image handler"))


(def handler
  (make-handler ["/" {"sources/" {"" sources-collection-handler
                                  "start" source-action-handler
                                  "stop" source-action-handler
                                  "fetch" source-action-handler
                                  [:id] source-handler}

                      "images/" {"" images-collection-handler
                                [:id] image-handler}}]))

(def app
  (-> handler
      wrap-params
      wrap-trace :header :ui))

(defn -main
  "I don't do a whole lot...yet."
  [& args]
  (println "Hello, World!"))
