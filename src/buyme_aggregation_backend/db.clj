(ns buyme-aggregation-backend.db
  (:require [mount.core :refer [defstate]]
            [conman.core :as conman]

            [buyme-aggregation-backend.conf :refer [config]]))

(defn pool-spec [config]
  {:adapter     "postgresql"
   ;:init-size  1
   ;:minimum-idle   1
   ;:maximum-idle   4
   ;:max-active 32
   :jdbc-url    "jdbc:postgresql://localhost:5432/"
   :username    (config :database-user)
   :password    (config :database-password)
   :port-number (Integer. (config :database-port))})


(defstate ^:dynamic *db*
          :start (conman/connect! (pool-spec config))
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/sources.sql")