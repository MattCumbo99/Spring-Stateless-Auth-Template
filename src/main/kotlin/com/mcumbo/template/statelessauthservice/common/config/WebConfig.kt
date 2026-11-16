package com.mcumbo.template.statelessauthservice.common.config

import com.mcumbo.template.statelessauthservice.common.logging.RequestLoggingInterceptor
import com.mcumbo.template.statelessauthservice.common.properties.CorsProps
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val corsProps: CorsProps,
    private val requestLoggingInterceptor: RequestLoggingInterceptor,
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedOriginPatterns(*corsProps.allowedOriginPatterns.toTypedArray())
            .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(requestLoggingInterceptor).addPathPatterns("/**")
    }
}
