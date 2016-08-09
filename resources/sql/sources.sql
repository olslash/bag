-- :name get-all-sources :? :*
-- :doc retrieve all sources.
SELECT * FROM sources

-- :name insert-source! :! :n
-- :doc retrieve all sources.
INSERT INTO sources (name, type, url_root)
VALUES (:name, :type, :url_root)

