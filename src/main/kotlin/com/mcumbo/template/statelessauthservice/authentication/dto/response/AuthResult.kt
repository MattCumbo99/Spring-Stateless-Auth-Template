package com.mcumbo.template.statelessauthservice.authentication.dto.response

import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse

data class AuthResult(
    val userData: UserResponse,
    val sessionJwt: String,
    val refreshJwt: String,
)
