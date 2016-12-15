(ns buyme-aggregation-backend.helpers.lambda
  (:require [amazonica.aws.lambda :refer [invoke]]
            [amazonica.aws.s3transfer :refer [upload]]
            [cheshire.core :as json]
            [clojure.java.io :as io])


  (:import (java.nio.charset StandardCharsets)))

(defn lambda-handler [in out ctx handler-fn]
  (let [event (json/parse-stream (io/reader in) true)
        res   @(handler-fn event ctx)]
    (with-open [w (io/writer out)]
      (json/generate-stream res w))))

(defn invoke-lambda-fn [fn-name payload]
  (let [res  (invoke {:endpoint "us-west-1"}
                     :function-name fn-name
                     :payload (json/generate-string payload))
        json (-> (String. (.array (:payload res)) StandardCharsets/UTF_8)
                 (json/parse-string true))]
    ;; can be :errorMessage
    ;    :body
    ;    :result)]
    ;(json/parse-string true)
    ;(update :body #(json/parse-string % true)))]
    json))

(defn upload-s3-file
  ([bucket-name file-name input-stream user-metadata]
   (let [content-length (.available input-stream)]
     (upload {:endpoint      "us-west-1"
              :client-config {:max-error-retry 10
                              :socket-timeout 20000
                              :connection-timeout 20000
                              :request-timeout 20000
                              :client-execution-timeout 20000}}
             bucket-name
             file-name
             input-stream
             {:content-length content-length
              :user-metadata  user-metadata})))
  ([bucket-name file-name input-stream] (upload-s3-file bucket-name file-name input-stream {})))

