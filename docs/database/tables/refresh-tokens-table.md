# Table: refresh_tokens

## Schema

| Column Name | Datatype      | Nullable | Default Value | Description                               |
|-------------|---------------|----------|---------------|-------------------------------------------|
| jti         | PK `UUID`     | No       |               | ID of JWT.                                |
| user_id     | FK `UUID`     | No       |               | User ID of user who owns this token.      |
| family_id   | `UUID`        | No       |               | Token lineage identifier.                 |
| token_hash  | `TEXT`        | No       |               | Hashed value of JWT.                      |
| status      | `VARCHAR(20)` | No       | ACTIVE        | Current status of token.                  |
| created_at  | `TIMESTAMPTZ` | No       | `now()`       | Creation date.                            |
| expires_at  | `TIMESTAMPTZ` | No       |               | Date and time of when this token expires. |

## Purpose
Holds authorization for refresh tokens to determine which sessions are valid.

## Constraints
1. `status` must be one of the following values: *ACTIVE*, *ROTATED*, *REVOKED*.

## Indexes
1. `idx_refresh_tokens_family_status` - Pairs `family_id` and `status`.
2. `idx_refresh_tokens_expires_at` - Index on `expires_at`.

## Scheduled Jobs
Entities are removed periodically, should the `expires_at` field be after the current time.

## Operational Notes
Entries are cascaded if the associated `user_id` is deleted.
