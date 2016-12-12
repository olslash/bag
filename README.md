# buyme-aggregation-backend

# Dev
* Fill in profiles.clj with your info


## Run a local dev database (docker)
* docker run --name bag-postgres -e POSTGRES_PASSWORD=pw -d -p 5432:5432 postgres
* lein migratus migrate

## get the repl going
* lein repl
* (start) / (stop) -- boot/shutdown app
* to reload after code changes: `(refresh-all)` (then `(start)` again)

# Prod
mostly TBD -- uberjar?
* should get config vars from the environment:

* provide a .env with overrides for the variables in profiles.clj
 (can be UPPER_SNAKE_CASE if desired)
* `source .env`

## Lambda stuff
* lambda functions go in the `lambda.*` namespace (see examples)
* run `sh script/refresh-lambda-scripts.sh` to generate a lambda jar and upload
it to aws as individual functions (need awscli tools installed)
* add your handlers to the `handlers` array in script/lambda_management_config.conf

## License

Copyright © 2016 Mitch Robb

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
