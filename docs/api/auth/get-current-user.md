# API: Get Current Authenticated User
Retrieves user details regarding the currently authenticated user.

## Endpoint
`GET /api/v1/auth/me`

## Authentication
Requires an authenticated user via session cookies.

## Responses
### `200 OK` - Success
Gets the user details related to the provided authentication session cookie.

*(See [User Response Body](../response-schemas/user-response.md) for return type.)*

### Error Responses
| Status Code        | Error Code     | Description                                                        |
|--------------------|----------------|--------------------------------------------------------------------|
| `401 Unauthorized` | `UNAUTHORIZED` | Details on the current user could not be retrieved for any reason. |

*(See [Error Response Body](../response-schemas/error-response.md) for return type.)*
