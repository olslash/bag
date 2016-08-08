(ns buyme-aggregation-backend.routes.images.controller
  (:require [liberator.core :refer [resource]]))

(def images-collection-handler
  (resource
    :available-media-types ["text/plain"]
    :handle-ok "this is the images handler"))

(def image-handler
  (resource
    :available-media-types ["text/plain"]
    :handle-ok "this is the single image handler"))
