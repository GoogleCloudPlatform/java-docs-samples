CREATE TABLE votes (
  vote_id     SERIAL PRIMARY KEY,
  time_cast   TIMESTAMP    NOT NULL,
  candidate   CHAR(6)      NOT NULL,
  uid         VARCHAR(128) NOT NULL
);