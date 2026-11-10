package com.mcumbo.template.statelessauthservice.authentication.filter

import com.mcumbo.template.statelessauthservice.authentication.provider.CustomUserDetailsService
import com.mcumbo.template.statelessauthservice.authentication.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val customUserDetailsService: CustomUserDetailsService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        TODO("Not yet implemented")
    }
}
