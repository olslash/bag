(ns lambda.fetch-store-image
  (:require [org.httpkit.client :as http]
            [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(defn handle-event
  [{:strs [url headers]}]
  (let [response @(http/get url {:headers headers})]
    {:statusCode 200
     :headers    {}
     :body       {:ok   true
                  :result response}}))

(deflambdafn lambda.fetch-store-image.handler [in out ctx]
  (let [event (json/read (io/reader in))
        res (handle-event event)]
    (with-open [w (io/writer out)]
      (json/write res w))))
