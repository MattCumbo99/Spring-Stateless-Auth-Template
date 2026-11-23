# API: Get User by Username

## Endpoint
`GET /api/v1/users/{username}`

### Path Parameters
| Name       | Type     | Required | Description           |
|------------|----------|----------|-----------------------|
| `username` | `string` | Yes      | User's unique handle. |

## Authentication
Publicly accessible.

## Responses
### `200 OK` - Success
Returns when a user exists with the given username.

*(See [User Response Body](../response-schemas/user-response.md) for return type.)*

### Error Responses
| Status Code     | Error Code       | Description                         |
|-----------------|------------------|-------------------------------------|
| `404 Not Found` | `USER_NOT_FOUND` | No existing user has this username. |

*(See [Error Response Body](../response-schemas/error-response.md) for return type.)*
