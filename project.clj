(defproject buyme-aggregation-backend "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-environ "1.0.3"]
            [migratus-lein "0.4.0"]]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/algo.generic "0.1.2"]

                 [environ "1.0.3"]

                 [clj-http "2.2.0"]
                 [com.cemerick/url "0.1.1"]
                 [liberator "0.14.1"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-devel "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [bidi "2.0.8"]
                 [jarohen/chime "0.1.9"]
                 [clj-time "0.11.0"]

                 [org.postgresql/postgresql "9.4.1209"]
                 [conman "0.5.8"]
                 [migratus "0.8.27"]
                 [robert/hooke "1.3.0"]

                 ;; logging
                 [com.taoensso/timbre "4.7.0"]
                 [com.fzakaria/slf4j-timbre "0.3.2"] ;; req'd by migratus


                 [mount "0.1.10"]
                 [prismatic/schema "1.1.2"]


                 [compojure "1.0.2"]] ;; req'd by liberator.dev


  ;:dev-dependencies [[clojure.tools.namespace "0.2.11"]]

  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "org.postgresql.Driver"
                  :subprotocol "postgresql"
                  :subname "//localhost:5432/postgres"
                  :user "postgres"
                  :password (or
                              (get (System/getenv) "DATABASE_PASSWORD")
                              (let [{{:keys [env]} :dev} (-> "profiles.clj" slurp read-string)]
                                (:database-password env)))}}




  :main ^:skip-aot buyme-aggregation-backend.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :repl-options {:init-ns user})
