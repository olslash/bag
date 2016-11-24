(ns lambda.fetch-store-image
  (:require [uswitch.lambada.core :refer [deflambdafn]]
            [clojure.data.json :as json]
            [clojure.java.io :as io])
  #_(:gen-class
      :methods [^:static [handler [String] String]]))

(defn handle-event
  [event]
  (println "Got the following event: " (pr-str event))
  {:statusCode 200
   :headers {}
   :body "ok"})

(deflambdafn lambda.fetch-store-image.handler [in out ctx]
  (let [event (json/read (io/reader in))
        res (handle-event event)]
    (with-open [w (io/writer out)]
      (json/write res w))))
