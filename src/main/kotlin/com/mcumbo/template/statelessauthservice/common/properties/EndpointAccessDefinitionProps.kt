package com.mcumbo.template.statelessauthservice.common.properties

import com.mcumbo.template.statelessauthservice.common.model.SecurityEndpointDefinition
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.security")
data class EndpointAccessDefinitionProps(
    val anonymousEndpoints: List<SecurityEndpointDefinition> = emptyList(),
    val publicEndpoints: List<SecurityEndpointDefinition> = emptyList(),
)
