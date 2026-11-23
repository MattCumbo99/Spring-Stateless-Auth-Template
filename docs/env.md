# Environment Variables
Contains specifications on which environment variables are required for the application to run.

<!-- TOC -->
* [Environment Variables](#environment-variables)
  * [Profiles](#profiles)
    * [DEV (Development)](#dev-development)
    * [PROD (Production)](#prod-production)
<!-- TOC -->

## Profiles
### DEV (Development)
Default profile.

| Variable Name        | Default Value              | Description                                                                 |
|----------------------|----------------------------|-----------------------------------------------------------------------------|
| `DB_URL`             |                            | URL of database to connect to.                                              |
| `DB_USERNAME`        |                            | Database username to connect with.                                          |
| `DB_PASSWORD`        |                            | Password of database account.                                               |
| `JWT_REFRESH_SECRET` | *(Generated JWT Secret 1)* | Secret to use when creating JSON Web Tokens for refreshing credentials.     |
| `JWT_SESSION_SECRET` | *(Generated JWT Secret 2)* | Secret to use when creating JSON Web Tokens for general application access. |

### PROD (Production)
```
SPRING_PROFILES_ACTIVE=prod
```

| Variable Name                | Default Value | Description                                                                                                                                                                         |
|------------------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CORS_ORIGIN_PATTERNS`       |               | Comma-separated list of allowed frontend origin patterns. Supports wildcards.                                                                                                       |
| `CRON_REFRESH_TOKEN_CLEANUP` | `0 0 3 * * ?` | [Cron](https://docs.oracle.com/cd/E12058_01/doc/doc.1014/e12030/cron_expressions.htm) specification of when the refresh token cleanup job should run. (_Default: Everyday at 3am._) |
| `DB_URL`                     |               | URL of database to connect to.                                                                                                                                                      |
| `DB_USERNAME`                |               | Database username to connect with.                                                                                                                                                  |
| `DB_PASSWORD`                |               | Password of database account.                                                                                                                                                       |
| `JWT_REFRESH_SECRET`         |               | Secret to use when creating JSON Web Tokens for refreshing credentials.                                                                                                             |
| `JWT_SESSION_SECRET`         |               | Secret to use when creating JSON Web Tokens for general application access.                                                                                                         |
