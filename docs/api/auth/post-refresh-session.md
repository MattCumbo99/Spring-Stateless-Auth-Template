# API: Refresh Current Session
Rotates both session and refresh cookies to restore authentication state.

## Endpoint
`POST /api/v1/auth/refresh`

## Authentication
Requires a valid refresh cookie.

## Request Body Parameters
This endpoint does not use any request body.

## Responses
### `200 OK` - Success
Attaches new session and refresh cookies in the response.

No response body is returned.

### Error Responses
| Status Code        | Error Code        | Description                                                                                                |
|--------------------|-------------------|------------------------------------------------------------------------------------------------------------|
| `401 Unauthorized` | `INVALID_SESSION` | The provided authentication details could not be validated OR an internal security incident was triggered. |

*(See [Error Response Body](../response-schemas/error-response.md) for return type.)*
