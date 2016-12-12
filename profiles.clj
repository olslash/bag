{:dev {:env {:environment           "development"
             :database-name         "postgres"
             :database-password     "pw"
             :database-port         "5432"
             :database-user         "postgres"
             :database-subname      "//localhost:5432/postgres"
             :imgur-client-id       "your-id"
             :aws-lambda-exec-role  "arn:aws:iam::fixme:role/lambda-exec"
             :aws-access-key-id     "your-access-key"
             :aws-secret-access-key "your-secret-key"
             :log-level             "info"}}}

:test {:env {:environment       "production"
             :database-name     "postgres"
             :database-password "pw"
             :database-port     "5432"
             :database-user     "postgres"
             :database-subname  "//localhost:5432/postgres"
             :log-level         "debug"}}
