{:dev  {:env {:environment "development"
              :database-name "postgres"
              :database-password "pw"
              :database-port "5432"
              :database-user "postgres"
              :log-level "info"}}

 :prod {:env {:environment "production"
              :database-port "5432"
              :log-level "debug"}}}