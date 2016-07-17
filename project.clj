(defproject buyme-aggregation-backend "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-ring "0.9.7"]
            [lein-environ "1.0.3"]
            [migratus-lein "0.4.0"]]

  :dependencies [[org.clojure/clojure "1.8.0"]

                 [environ "1.0.3"]

                 [liberator "0.14.1"]
                 [ring/ring-core "1.5.0"]
                 [bidi "2.0.8"]
                 [jarohen/chime "0.1.9"]
                 [clj-time "0.11.0"]

                 [org.postgresql/postgresql "9.4.1209"]
                 [conman "0.5.8"]
                 [migratus "0.8.27"]

                 ;; logging
                 [com.taoensso/timbre "4.6.0"]
                 [com.fzakaria/slf4j-timbre "0.3.2"]          ;; req'd by migratus



                 ;; schema
                 ;; mount
                 ]

  :migratus {:store :database
             :migration-dir "migrations"
             :db {:classname "org.postgresql.Driver"
                  :subprotocol "postgresql"
                  :subname "//localhost:5432/wp_dev"
                  :user "postgres"
                  :password "pw"}}


  :main ^:skip-aot buyme-aggregation-backend.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :ring {:handler buyme-aggregation-backend.core/app})
