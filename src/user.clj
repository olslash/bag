(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [mount.core :as mount]
            [buyme-aggregation-backend.core :as app]))

(defn fix-ns []
  (ns-unalias (find-ns 'user) 'app))

(defn start []
  (app/-main))

(defn stop []
  (mount/stop))

(defn refresh []
  (stop)
  (fix-ns)
  (tn/refresh))

(defn refresh-all []
  (stop)
  (fix-ns)
  (tn/refresh-all))

(defn go
  "starts all states defined by defstate"
  []
  (start)
  :ready)

(defn reset
  "stops all states defined by defstate, reloads modified source files, and restarts the states"
  []
  (stop)
  (fix-ns)
  (tn/refresh :after 'user/go))

(tn/set-refresh-dirs "./src/buyme_aggregation_backend")
