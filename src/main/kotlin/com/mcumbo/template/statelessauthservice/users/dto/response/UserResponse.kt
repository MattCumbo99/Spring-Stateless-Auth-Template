package com.mcumbo.template.statelessauthservice.users.dto.response

import com.mcumbo.template.statelessauthservice.users.entity.User
import java.time.OffsetDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val username: String,
    val createdAt: OffsetDateTime,
)

fun User.toResponse(): UserResponse =
    UserResponse(
        id = this.id!!,
        username = this.username,
        createdAt = this.createdAt,
    )
