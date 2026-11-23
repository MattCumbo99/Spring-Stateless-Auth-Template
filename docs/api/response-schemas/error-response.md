# Response Schema: Error

| Property Name | Datatype | Format | Optional | Nullable | Description                    |
|---------------|----------|--------|----------|----------|--------------------------------|
| `detail`      | `string` |        | No       | No       | Error message.                 |
| `instance`    | `string` |        | No       | No       | URI which triggered the error. |
| `status`      | `number` |        | No       | No       | HTTP status code.              |
| `title`       | `string` |        | No       | No       | Short summary of the error.    |
| `errorCode`   | `string` |        | No       | No       | Specific error type.           |
| `timestamp`   | `object` |        | No       | No       | Error time details.            |

## Example Body
```json
{
  "detail": "User 'admin' not found.",
  "instance": "/api/v1/users/admin",
  "status": 404,
  "title": "Not Found",
  "errorCode": "USER_NOT_FOUND",
  "timestamp": {
    "epochSeconds": 1795214307,
    "nanosecondsOfSecond": 427742226
  }
}
```
