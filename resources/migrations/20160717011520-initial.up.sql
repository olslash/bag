CREATE TABLE IF NOT EXISTS sources (
  id SERIAL PRIMARY KEY,
  source_impl_id varchar(80),
  name varchar(80) UNIQUE
);

--;;

CREATE TABLE IF NOT EXISTS source_settings (
  source_id integer REFERENCES sources ON DELETE CASCADE,
  key varchar(80),
  value text,
  CONSTRAINT unique_keys_per_id UNIQUE (source_id, key)
);

--;;

CREATE TABLE IF NOT EXISTS images (
  source_id integer REFERENCES sources,
  image_id integer PRIMARY KEY,
  s3_url varchar(1000),
  slug varchar(200),
  title text,
  description text,
  attribution_id text,
  attribution_name text,
  attribution_url text,
  width integer,
  height integer,
  type varchar(200), -- like image/png
  nsfw boolean,
  ingested_at timestamptz default CURRENT_TIMESTAMP
)