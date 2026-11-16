package com.mcumbo.template.statelessauthservice.users.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterUserRequest(
    @field:NotBlank(message = "Username is required.")
    @field:Pattern(
        regexp = "^[a-z0-9_]{3,25}$",
        message =
            "Username must contain only letters, numbers, underscores and between 3 and 25 characters.",
    )
    val username: String,
    @field:NotBlank(message = "Password is required.")
    @field:Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters.")
    val password: String,
)
