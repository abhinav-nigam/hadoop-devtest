-- Database create script
CREATE DATABASE crawler_db
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       CONNECTION LIMIT = -1;

\connect crawler_db

-- Table sitemap_urls_queue create script
CREATE TABLE sitemap_urls_queue
(
  url character varying NOT NULL,
  gz_timestamp timestamp with time zone,
  status integer, -- Not Started - 1...
  id bigserial NOT NULL,
  CONSTRAINT sitemap_url_unique UNIQUE (url)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE sitemap_urls_queue
  OWNER TO postgres;
COMMENT ON COLUMN sitemap_urls_queue.status IS 'Not Started - 1
In Progress - 2
Successful - 3
Failed - 4';

-- Table listings_urls_queue create script
CREATE TABLE listings_urls_queue
(
  id bigserial NOT NULL,
  url character varying,
  status integer -- Not Started - 1...
)
WITH (
  OIDS=FALSE
);
ALTER TABLE listings_urls_queue
  OWNER TO postgres;
COMMENT ON COLUMN listings_urls_queue.status IS 'Not Started - 1
Inprogress - 2
successful - 3
Failed -4';

