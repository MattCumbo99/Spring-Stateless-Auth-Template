# Table: users

## Schema

| Column Name     | Datatype      | Nullable | Default Value       | Description         |
|-----------------|---------------|----------|---------------------|---------------------|
| `id`            | PK `UUID`     | No       | `gen_random_uuid()` | Primary identifier. |
| `username`      | `VARCHAR(25)` | No       |                     | Username.           |
| `password_hash` | `TEXT`        | No       |                     | Hashed password.    |
| `created_at`    | `TIMESTAMPTZ` | No       | `now()`             | Creation date.      |
| `updated_at`    | `TIMESTAMPTZ` | No       | `now()`             | Last updated date.  |

## Purpose
Stores information and credentials on users who use the app.

## Constraints
1. `username`:
   1. Must be **unique**.
   2. Must match against this regex: `^[a-z0-9_]{3,25}$`

## Indexes
None.

## Scheduled Jobs
None.

## Operational Notes
None.
