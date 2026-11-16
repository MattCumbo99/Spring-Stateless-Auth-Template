package com.mcumbo.template.statelessauthservice.authentication.filter

import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidSessionException
import com.mcumbo.template.statelessauthservice.authentication.properties.CookieProps
import com.mcumbo.template.statelessauthservice.authentication.provider.CustomUserDetailsService
import com.mcumbo.template.statelessauthservice.authentication.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val customUserDetailsService: CustomUserDetailsService,
    private val cookieProps: CookieProps,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val sessionJwt = getSessionTokenFromCookies(request)

        if (sessionJwt != null) {
            val sessionUsername =
                jwtService.getSessionUsername(sessionJwt) ?: throw InvalidSessionException()

            val userDetails = customUserDetailsService.loadUserByUsername(sessionUsername)
            val authToken =
                UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities,
                )

            // Set the authentication holder with the session data
            SecurityContextHolder.getContext().authentication = authToken
        }

        filterChain.doFilter(request, response)
    }

    private fun getSessionTokenFromCookies(request: HttpServletRequest): String? =
        request.cookies?.firstOrNull { it.name == cookieProps.sessionTokenName }?.value
}
