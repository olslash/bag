(ns buyme-aggregation-backend.routes
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]

            [mount.core :refer [defstate]]

            [bidi.ring :refer [make-handler]]
            [liberator.dev :refer [wrap-trace]]

            [buyme-aggregation-backend.conf :refer [config]]
            [buyme-aggregation-backend.routes.sources.controller :as sources-controller]
            [buyme-aggregation-backend.routes.images.controller :as images-controller]))



(def handler
  (make-handler ["/" {"sources/" {""      sources-controller/sources-collection-handler
                                  "start" sources-controller/source-action-handler
                                  "stop"  sources-controller/source-action-handler
                                  "fetch" sources-controller/source-action-handler
                                  [:id]   sources-controller/source-handler}

                      "images/"  {""    images-controller/images-collection-handler
                                  [:id] images-controller/image-handler}}]))
(defn start-server [config]
  (-> handler
      wrap-params
      (wrap-trace :header :ui)
      (run-jetty {:join? false :port 8080})))

(defstate webapp
          :start (start-server config)
          :stop (.stop webapp))
