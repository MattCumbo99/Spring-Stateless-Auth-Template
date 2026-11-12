package com.mcumbo.template.statelessauthservice.common.config

import com.mcumbo.template.statelessauthservice.authentication.filter.JwtAuthFilter
import com.mcumbo.template.statelessauthservice.authentication.provider.CustomUserDetailsService
import com.mcumbo.template.statelessauthservice.common.properties.CorsProps
import com.mcumbo.template.statelessauthservice.common.properties.EndpointAccessDefinitionProps
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig {
    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(corsProps: CorsProps): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOriginPatterns = corsProps.allowedOriginPatterns
        config.allowCredentials = true
        config.allowedHeaders = listOf("*")
        config.allowedMethods = listOf("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)

        return source
    }

    @Bean
    fun authenticationProvider(
        userDetailsService: CustomUserDetailsService,
        passwordEncoder: PasswordEncoder,
    ): AuthenticationProvider {
        val provider = DaoAuthenticationProvider(userDetailsService)
        provider.setPasswordEncoder(passwordEncoder)

        return provider
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter,
        authenticationProvider: AuthenticationProvider,
        endpointAccessDefinitionProps: EndpointAccessDefinitionProps,
        corsProps: CorsProps,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource(corsProps)) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authCfg ->
                endpointAccessDefinitionProps.anonymousEndpoints.forEach { (method, path) ->
                    authCfg.requestMatchers(method, path).anonymous()
                }

                endpointAccessDefinitionProps.publicEndpoints.forEach { (method, path) ->
                    authCfg.requestMatchers(method, path).permitAll()
                }

                // Authenticate all other endpoints by default
                authCfg.anyRequest().authenticated()
            }.authenticationProvider(authenticationProvider)
            .addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter::class.java,
            ).exceptionHandling { exceptionCfg ->
                exceptionCfg.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                }
            }

        return http.build()
    }
}
