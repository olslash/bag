(ns buyme-aggregation-backend.helpers.lambda
  (:require [amazonica.aws.lambda :refer [invoke]]
            [cheshire.core :as json])
  (:import (java.nio.charset StandardCharsets)))


(defn invoke-lambda-fn [fn-name payload]
  (let [res (invoke {:endpoint "us-west-1"}
                    :function-name fn-name
                    :payload payload)
        json (-> (String. (.array (:payload res)) StandardCharsets/UTF_8)
                 (json/parse-string true)
                 (:body)
                 (:result)
                 (json/parse-string true)
                 (update :body #(json/parse-string % true)))]
    json))
