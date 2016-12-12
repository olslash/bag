(ns lambda.fetch-store-image
  (:require [buyme-aggregation-backend.helpers.lambda :refer [lambda-handler upload-s3-file]]
            [org.httpkit.client :as http]
            [uswitch.lambada.core :refer [deflambdafn]]))


(defn handle-event
  [{:keys [image-url
           image-fetch-headers
           bucket-name
           file-name
           image-meta]}
   ctx]
  (let [image           (:body @(http/get image-url {:headers (or image-fetch-headers {})
                                                     :as      :stream}))
        s3-upload       (upload-s3-file bucket-name file-name image (or image-meta {}))
        out             (promise)
        add-listener-fn (:add-progress-listener s3-upload)]
    (add-listener-fn
      #(when (= :completed (:event %)
                (deliver out {:statusCode 200
                              :headers    {}
                              :body       {:ok     true
                                           :result ((:upload-result s3-upload))}}))))

    out))



(deflambdafn lambda.fetch-store-image.handler [in out ctx]
  (lambda-handler in out ctx handle-event))
