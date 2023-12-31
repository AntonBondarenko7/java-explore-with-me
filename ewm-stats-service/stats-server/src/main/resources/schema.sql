DROP TABLE IF EXISTS hits;

CREATE TABLE IF NOT EXISTS hits (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  app VARCHAR(64) NOT NULL,
  uri VARCHAR(256) NOT NULL,
  ip VARCHAR(64) NOT NULL,
  timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  CONSTRAINT pk_hit PRIMARY KEY (id)
);