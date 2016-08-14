-- :name get-all-sources :? :*
-- :doc retrieve all sources.
SELECT * FROM sources

-- :name insert-source! :! :n
-- :doc insert a new source.
INSERT INTO sources (source_impl_id, name, url_root)
VALUES (:source_impl_id, :name, :url_root)

-- :name remove-source! :! :n
-- :doc remove a source by id
DELETE FROM sources
WHERE id = :id

-- :name upsert-setting! :! :n
-- :doc insert/update a source setting
INSERT INTO source_settings (id, key, value)
VALUES (:source_id, :key, :value)
ON CONFLICT (id, key)
DO UPDATE SET value = EXCLUDED.value
