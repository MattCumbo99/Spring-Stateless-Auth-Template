# Response Schema: User

| Property Name | Datatype | Format     | Optional | Nullable | Description                              |
|---------------|----------|------------|----------|----------|------------------------------------------|
| `id`          | `string` | `uuid`     | No       | No       | Primary user identifier.                 |
| `username`    | `string` |            | No       | No       |                                          |
| `created_at`  | `string` | `iso-8601` | No       | No       | Date and time when the user was created. |

## Example Body
```json
{
  "id": "2335aaba-75bb-4bed-a414-37dfe6748b4a",
  "username": "john_smith",
  "created_at": "2026-11-20T21:12:00Z"
}
```
