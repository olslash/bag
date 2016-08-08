(ns buyme-aggregation-backend.db
  (:require [mount.core :refer [defstate]]
            [conman.core :as conman]

            [buyme-aggregation-backend.conf :refer [config]]))

(defn pool-spec []
  {:subprotocol    :postgresql
   :init-size  1
   :min-idle   1
   :max-idle   4
   :max-active 32
   :jdbc-url (str "jdbc:postgresql://localhost:5432/"
                  (config :database-name)
                  "?user="
                  (config :database-user)
                  "&password="
                  (config :database-password))})

(defstate ^:dynamic *db*
          :start (conman/connect! (pool-spec))
          :stop (conman/disconnect! *db*))

(conman/bind-connection *db* "sql/queries.sql")