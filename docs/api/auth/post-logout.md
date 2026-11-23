# API: Logout
Clears authentication cookies from the response and invalidates the user's current session.

## Endpoint
`POST /api/v1/auth/logout`

## Authentication
Publicly accessible.

## Request Body Parameters
This endpoint does not use any request body.

## Responses
### `204 NO CONTENT` - Success
Always succeeds. Attaches instant-expiring session cookies to the response.

No response body is returned.
