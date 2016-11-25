{:dev  {:env {:environment "development"
              :database-name "postgres"
              :database-password "pw"
              :database-port "5432"
              :database-user "postgres"
              :database-subname "//localhost:5432/postgres"
              :log-level "info"}}

 :test {:env {:environment "production"
              :database-port "5432"
              :database-subname "//localhost:5432/fixme"
              :log-level "debug"}}}
