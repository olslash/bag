(ns buyme-aggregation-backend.sources.index
  (:require [buyme-aggregation-backend.sources.imgur :as imgur]))


(def source-impls {"Imgur" imgur/make-source})