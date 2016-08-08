{:dev  {:env {:environment "development"
              :database-password "pw"
              :database-port 5432
              :timbre {:level :info }
              }}
 :prod {:env {:environment "production"
              :database-port 5432}}}