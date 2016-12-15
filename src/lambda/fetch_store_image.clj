(ns lambda.fetch-store-image
  (:require [buyme-aggregation-backend.helpers.lambda :refer [lambda-handler upload-s3-file]]
            [buyme-aggregation-backend.util.misc :refer [trunc]]

            [clojure.string :as str]
            [org.httpkit.client :as http]
            [uswitch.lambada.core :refer [deflambdafn]]))


(defn handle-event
  [{:keys [image-url
           image-fetch-headers
           bucket-name
           file-name
           image-meta]}
   ctx]
  (let [sanitized-meta  (-> image-meta
                            (update :description #(when % (trunc % 120))) ;; max length description
                            (update :description #(when % (str/replace % #"[^a-zA-Z]" ""))) ;; no special characters
                            (update :title #(when % (str/replace % #"[^a-zA-Z]" ""))))
        image           (:body @(http/get image-url {:headers (or image-fetch-headers {})
                                                     :as      :stream}))
        s3-upload       (upload-s3-file bucket-name (str/replace file-name #"[^a-zA-Z]" "") image (or sanitized-meta {}))
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
