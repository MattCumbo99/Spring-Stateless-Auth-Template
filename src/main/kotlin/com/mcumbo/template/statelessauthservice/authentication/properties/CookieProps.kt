package com.mcumbo.template.statelessauthservice.authentication.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security.cookies")
data class CookieProps(
    val sessionTokenName: String,
    val refreshTokenName: String,
    val sameSiteStrategy: String,
)
