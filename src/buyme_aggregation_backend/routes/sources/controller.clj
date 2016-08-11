(ns buyme-aggregation-backend.routes.sources.controller
  (:require [liberator.core :refer [resource]]
            [conman.core :as conman]
            [buyme-aggregation-backend.db :as db]
            [buyme-aggregation-backend.helpers.liberator :refer [check-content-type parse-json]]
            ))

(def sources-collection-handler
  (resource
    :available-media-types ["application/json"]
    :allowed-methods [:get :post]
    :known-content-type? #(check-content-type % ["application/json"])
    :malformed? #(parse-json % ::data)
    :post! #(println (::data %))
    :handle-ok db/get-all-sources))

(def source-handler
  (resource
    :available-media-types ["text/plain"]
    :handle-ok "this is the single source handler"))

(def source-action-handler
  (resource
    :available-media-types ["text/plain"]
    :handle-ok "this is the source action handler"))
