-- :name get-all-sources :? :*
-- :doc retrieve all sources.
SELECT * FROM sources

-- :name insert-source! :! :n
-- :doc retrieve all sources.
INSERT INTO sources (name, type, url_root)
VALUES (:name, :type, :url_root)

-- :name upsert-setting! :! :n
-- :doc insert/update a source setting
INSERT INTO source_settings (source_id, key, value)
VALUES (:source_id, :key, :value)
ON CONFLICT (source_id, key)
DO UPDATE SET value = EXCLUDED.value
