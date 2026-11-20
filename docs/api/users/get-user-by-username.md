# API: Get User by Username

## Endpoint
`GET /api/v1/users/{username}`

## Responses
### `200 OK` - Success
Returns when a user exists with the given username.

See the [User Response Schema](../response-schemas/user-response.md) for details on response body.

### Error Responses

| Status Code     | Error Code       | Description                         |
|-----------------|------------------|-------------------------------------|
| `404 Not Found` | `USER_NOT_FOUND` | No existing user has this username. |
