package com.mcumbo.template.statelessauthservice.authentication.dto.response

data class RefreshResult(
    val sessionJwt: String,
    val refreshJwt: String,
)
