(ns buyme-aggregation-backend.conf
  (:require [mount.core :as mount :refer [defstate]]
            [taoensso.timbre :refer [info]]
            [environ.core :refer [env]]
            ))



(defstate config
          :start env)
