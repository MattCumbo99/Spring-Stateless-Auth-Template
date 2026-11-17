package com.mcumbo.template.statelessauthservice.authentication.controller

import com.mcumbo.template.statelessauthservice.authentication.dto.request.LoginRequest
import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidSessionException
import com.mcumbo.template.statelessauthservice.authentication.service.AuthService
import com.mcumbo.template.statelessauthservice.authentication.service.CookieService
import com.mcumbo.template.statelessauthservice.common.annotation.RateLimit
import com.mcumbo.template.statelessauthservice.common.model.RateLimitPlan
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieService: CookieService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("/login")
    @RateLimit(RateLimitPlan.AUTH_STRICT)
    fun login(
        @RequestBody @Valid loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): UserResponse {
        val authResult = authService.loginUser(loginRequest)

        val sessionCookie = cookieService.createSessionCookie(authResult.sessionJwt)
        val refreshCookie = cookieService.createRefreshCookie(authResult.refreshJwt)

        // Attach the authentication cookies to the response
        response.addHeader(SET_COOKIE, sessionCookie.toString())
        response.addHeader(SET_COOKIE, refreshCookie.toString())

        val userData = authResult.userData

        // Log this event
        logger.info("Account login successful: ${userData.username} with IP: ${request.remoteAddr}")

        return userData
    }

    @GetMapping("/me")
    fun getCurrentUser(): UserResponse = authService.getCurrentUser()

    @PostMapping("/refresh")
    fun refreshSession(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val refreshJwt = cookieService.extractRefreshToken(request) ?: throw InvalidSessionException()

        val refreshResult = authService.refreshSession(refreshJwt)

        // Attach the new authentication cookies to the response
        val sessionCookie = cookieService.createSessionCookie(refreshResult.sessionJwt)
        val refreshCookie = cookieService.createRefreshCookie(refreshResult.refreshJwt)

        response.addHeader(SET_COOKIE, sessionCookie.toString())
        response.addHeader(SET_COOKIE, refreshCookie.toString())
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val refreshJwt = cookieService.extractRefreshToken(request)

        authService.logoutUser(refreshJwt)

        // Detach any existing session cookies
        cookieService.clearAuthCookies(response)
    }
}
