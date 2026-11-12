package com.mcumbo.template.statelessauthservice.common.model

import org.springframework.http.HttpMethod

data class SecurityEndpointDefinition(
    val method: HttpMethod,
    val path: String,
)
