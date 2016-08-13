(ns buyme-aggregation-backend.data
  (:require [taoensso.timbre :refer [info]]
            [clj-http.client :as client]))

(defn fetch-http [source]
  (let [url (:url_root source)]
    (info "Fetching from" source)
    (:body (client/get url))))
