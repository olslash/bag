CREATE TABLE IF NOT EXISTS sources (
  id SERIAL PRIMARY KEY,
  source_impl_id varchar(80),
  name varchar(80),
  url_root text
);

--;;

CREATE TABLE IF NOT EXISTS source_settings (
  source_id integer REFERENCES sources ON DELETE CASCADE,
  key varchar(80),
  value text,
  CONSTRAINT u_constraint UNIQUE (source_id, key)
);

--;;

CREATE TABLE IF NOT EXISTS images (
  image_id integer PRIMARY KEY,
  source_id integer REFERENCES sources,
  slug varchar(200),
  description text,
  attribution_url text,
  attribution_name text,
  posted_at timestamptz,
  ingested_at timestamptz default CURRENT_TIMESTAMP
)