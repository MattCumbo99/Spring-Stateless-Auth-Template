package com.mcumbo.template.statelessauthservice.authentication.service

import com.mcumbo.template.statelessauthservice.authentication.dto.request.LoginRequest
import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidCredentialsException
import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidSessionException
import com.mcumbo.template.statelessauthservice.authentication.properties.CookieProps
import com.mcumbo.template.statelessauthservice.authentication.provider.CustomUserDetails
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.dto.response.toResponse
import com.mcumbo.template.statelessauthservice.users.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val cookieProps: CookieProps,
    private val cookieService: CookieService,
    private val refreshTokenService: RefreshTokenService,
    private val passwordEncoder: PasswordEncoder,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun loginUser(
        loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): UserResponse {
        val user =
            userService.getUserEntityByUsername(loginRequest.username.lowercase())
                ?: throw InvalidCredentialsException()

        val passwordMismatch = !passwordEncoder.matches(loginRequest.password, user.passwordHash)
        if (passwordMismatch) {
            throw InvalidCredentialsException()
        }

        // Begin providing authentication details for the user
        val sessionCookie = cookieService.createSessionCookie(user)
        val refreshCookie = cookieService.createRefreshCookie(user)

        // Attach the cookies to the response
        response.addHeader("Set-Cookie", sessionCookie.toString())
        response.addHeader("Set-Cookie", refreshCookie.toString())

        // Log this event
        logger.info("[LOGIN] Account login successful: ${user.username} with IP: ${request.remoteAddr}")

        return user.toResponse()
    }

    fun getCurrentUser(): UserResponse {
        val auth = SecurityContextHolder.getContext().authentication
        val isValidSession = auth != null && auth.isAuthenticated && auth.principal is CustomUserDetails

        return if (isValidSession) {
            // Get the username from the auth context
            val username = (auth.principal as CustomUserDetails).username

            userService.getUserByUsername(username)
        } else {
            throw InvalidSessionException()
        }
    }

    fun refreshSession(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        // Treat grabbing any token data as a malformed session
        val refreshJwt = getRefreshTokenFromCookies(request) ?: throw InvalidSessionException()
        val familyId = jwtService.getRefreshFamilyId(refreshJwt) ?: throw InvalidSessionException()
        val username = jwtService.getRefreshUsername(refreshJwt) ?: throw InvalidSessionException()
        val user = userService.getUserEntityByUsername(username) ?: throw InvalidSessionException()

        // Rotate the existing refresh token
        refreshTokenService.rotateAndProtect(refreshJwt)

        // Renew the cookies
        val newRefreshCookie = cookieService.createRefreshCookie(user, familyId)
        val newSessionCookie = cookieService.createSessionCookie(user)

        // Attach the cookies to the cookie jar
        response.addHeader("Set-Cookie", newRefreshCookie.toString())
        response.addHeader("Set-Cookie", newSessionCookie.toString())
    }

    fun logoutUser(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val refreshJwt = getRefreshTokenFromCookies(request)

        if (refreshJwt != null) {
            // Leniently revoke tokens in the family
            jwtService.getRefreshFamilyId(refreshJwt)?.let { familyId ->
                refreshTokenService.revokeFamily(familyId)
            }
        }

        cookieService.clearAuthCookies(response)
    }

    private fun getRefreshTokenFromCookies(request: HttpServletRequest): String? =
        request.cookies?.firstOrNull { it.name == cookieProps.refreshTokenName }?.value
}
