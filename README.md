# buyme-aggregation-backend

# Dev

* docker run --name bag-postgres -e POSTGRES_PASSWORD=pw -d -p 5432:5432 postgres
* lein migratus migrate
* lein ring server

## License

Copyright © 2016 Mitch Robb

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
