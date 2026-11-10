package com.mcumbo.template.statelessauthservice.authentication.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.security.jwt")
data class JwtProps(
    val sessionSecret: String,
    val sessionExpiry: Duration,
    val refreshSecret: String,
    val refreshExpiry: Duration,
)
