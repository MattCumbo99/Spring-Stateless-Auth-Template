# API: Login
Attempts to authenticate user credentials and sends authorization cookies in the response.

## Endpoint
`POST /api/v1/auth/login`

## Authentication
Anonymous users only.

## Request Body Parameters
| Field      | Type     | Required | Nullable | Description               |
|------------|----------|----------|----------|---------------------------|
| `username` | `string` | Yes      | No       | Username to authenticate. |
| `password` | `string` | Yes      | No       | Password of user.         |

### Example Request Body
```json
{
  "username": "john_smith",
  "password": "simple123"
}
```

## Responses
### `200 OK` - Success
The provided username and password matched an existing user. Attaches authentication cookies related to the user
in the response.

*(See [User Response Body](../response-schemas/user-response.md) for return type.)*

### Error Responses
| Status Code        | Error Code            | Description                                                    |
|--------------------|-----------------------|----------------------------------------------------------------|
| `400 Bad Request`  | `INVALID_INPUT`       | One or more fields failed validation.                          |
| `401 Unauthorized` | `INVALID_CREDENTIALS` | A user with the provided authentication details was not found. |

*(See [Error Response Body](../response-schemas/error-response.md) for return type.)*
