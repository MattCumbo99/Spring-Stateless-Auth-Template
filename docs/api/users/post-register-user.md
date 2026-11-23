# API: Register User
Creates a new user account in the system and returns the newly generated user object.

## Endpoint
`POST /api/v1/users/register`

## Authentication
Anonymous users only.

## Request Body Parameters
| Field      | Type     | Required | Nullable | Description                                                                     |
|------------|----------|----------|----------|---------------------------------------------------------------------------------|
| `username` | `string` | Yes      | No       | Unique username (3-25 characters, lowercase alphanumeric and underscores only.) |
| `password` | `string` | Yes      | No       | Password (8-75 characters.)                                                     |

### Example Request
```json
{
  "username": "john_doe",
  "password": "simple7519"
}
```

## Responses
### `201 CREATED` - Success
Returns when the user was successfully registered.

*(See [User Response Body](../response-schemas/user-response.md) for return type.)*

### Error Responses
| Status Code       | Error Code      | Description                                   |
|-------------------|-----------------|-----------------------------------------------|
| `400 Bad Request` | `INVALID_INPUT` | One or more request fields failed validation. |
| `409 Conflict`    | `USER_CONFLICT` | A user already exists with this username.     |

*(See [Error Response Body](../response-schemas/error-response.md) for return type.)*
