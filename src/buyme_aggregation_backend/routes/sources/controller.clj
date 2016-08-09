(ns buyme-aggregation-backend.routes.sources.controller
  (:require [liberator.core :refer [resource]]
            [conman.core :as conman]
            [buyme-aggregation-backend.db :refer [*db*]]
            ))

(conman/bind-connection *db* "sql/sources.sql")

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
