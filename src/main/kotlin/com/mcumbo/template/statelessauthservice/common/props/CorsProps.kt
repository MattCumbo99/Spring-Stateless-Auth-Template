package com.mcumbo.template.statelessauthservice.common.props

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cors")
data class CorsProps(
    val allowedOriginPatterns: List<String> = emptyList(),
)
