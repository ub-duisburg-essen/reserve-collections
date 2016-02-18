CREATE TABLE access
(
  id SERIAL PRIMARY KEY,
  remotehost VARCHAR(128) NOT NULL,
  username VARCHAR(128),
  timestamp TIMESTAMP NOT NULL,
  virtualhost VARCHAR(256) NOT NULL,
  method VARCHAR(8) NOT NULL,
  requesturi VARCHAR(1024) NOT NULL,
  query VARCHAR(1024) NOT NULL,
  status SMALLINT NOT NULL,
  bytes INT NOT NULL,
  referer VARCHAR(1024),
  useragent VARCHAR(1024)
);