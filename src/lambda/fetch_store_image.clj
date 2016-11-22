(ns lambda.fetch-store-image
  (:gen-class
    :methods [^:static [handler [String] String]]))

(defn -handler [s]
  (str "Hello " s "!"))

