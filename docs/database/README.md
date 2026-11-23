# Database Overview

## Tables
1. [refresh_tokens](./tables/refresh-tokens-table.md)
2. [users](./tables/users-table.md)

## Flow Chart
```mermaid
erDiagram
    USERS
    REFRESH_TOKENS
    
    USERS ||--o{ REFRESH_TOKENS : owns
```
