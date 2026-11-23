# Authentication API
Handles user sessions and authorization.

## Endpoints
| Method | Path                   | Summary                                                               | Details                                      |
|--------|------------------------|-----------------------------------------------------------------------|----------------------------------------------|
| `GET`  | `/api/v1/auth/me`      | Gets the current authenticated user.                                  | [Get Current User](./get-current-user.md)    |
| `POST` | `/api/v1/auth/login`   | Attempts to authenticate the client against the provided credentials. | [Login](./post-login.md)                     |
| `POST` | `/api/v1/auth/logout`  | Removes any authentication details from the response.                 | [Logout](./post-logout.md)                   |
| `POST` | `/api/v1/auth/refresh` | Rotates and refreshes the current authentication cookies.             | [Refresh Session](./post-refresh-session.md) |
