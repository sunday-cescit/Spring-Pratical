-- create the games table (JPA’s ddl-auto=update would also do this, but explicit is safer)
CREATE TABLE IF NOT EXISTS games (
  id SERIAL PRIMARY KEY,
  name VARCHAR(30) NOT NULL UNIQUE,
  description TEXT NOT NULL,
  category VARCHAR(255) NOT NULL,
  price DOUBLE PRECISION NOT NULL,
  url VARCHAR(255) NOT NULL
);

-- insert a known row for test verification
INSERT INTO games (name, description, category, price, url)
VALUES (
  'Integration Test Game',
  'This game was inserted by Testcontainers init script.',
  'TestCategory',
  15.99,
  'http://example.com/itest'
);
