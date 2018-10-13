# Family Map Server

Family Map Server by Ashton Wiersdorf

## Database schema

```sql
CREATE TABLE accounts(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT DEFAULT(DATETIME('now')),
  updated_at TEXT DEFAULT(DATETIME('now')),
  username TEXT,
  password TEXT,
  first_name TEXT,
  last_name TEXT,
  email TEXT,
  gender TEXT,
  root_person INTEGER
);
CREATE TABLE auth_tokens(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT DEFAULT(DATETIME('now')),
  updated_at TEXT DEFAULT(DATETIME('now')),
  token TEXT,
  account_id INTEGER,
  expires TEXT
);
CREATE TABLE events(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT DEFAULT(DATETIME('now')),
  updated_at TEXT DEFAULT(DATETIME('now')),
  person_id INTEGER,
  account_id INTEGER,
  latitude NUMERIC,
  longitude NUMERIC,
  country TEXT,
  city TEXT,
  event_type TEXT,
  timestamp TEXT
);
CREATE TABLE people(
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  created_at TEXT DEFAULT(DATETIME('now')),
  updated_at TEXT DEFAULT(DATETIME('now')),
  first_name TEXT,
  last_name TEXT,
  gender TEXT,
  father INTEGER,
  mother INTEGER,
  spouse INTEGER,
  account_id INTEGER
);
```
