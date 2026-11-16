package com.mcumbo.template.statelessauthservice.authentication.service

import com.mcumbo.template.statelessauthservice.authentication.properties.CookieProps
import com.mcumbo.template.statelessauthservice.authentication.properties.JwtProps
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service

@Service
class CookieService(
    private val cookieProps: CookieProps,
    private val jwtProps: JwtProps,
) {
    fun createSessionCookie(sessionJwt: String): ResponseCookie = buildSessionCookie(sessionJwt)

    fun createRefreshCookie(refreshJwt: String): ResponseCookie = buildRefreshCookie(refreshJwt)

    fun extractRefreshToken(request: HttpServletRequest): String? =
        request.cookies?.firstOrNull { it.name == cookieProps.refreshTokenName }?.value

    fun clearAuthCookies(response: HttpServletResponse) {
        val blankSessionCookie = buildSessionCookie("").mutate().maxAge(0).build()

        val blankRefreshCookie = buildRefreshCookie("").mutate().maxAge(0).build()

        response.addHeader("Set-Cookie", blankSessionCookie.toString())
        response.addHeader("Set-Cookie", blankRefreshCookie.toString())
    }

    private fun buildSessionCookie(sessionToken: String) =
        ResponseCookie
            .from(cookieProps.sessionTokenName, sessionToken)
            .httpOnly(true)
            .secure(true)
            .sameSite(cookieProps.sameSiteStrategy)
            .path("/")
            .maxAge(jwtProps.sessionExpiry.seconds)
            .build()

    private fun buildRefreshCookie(refreshToken: String) =
        ResponseCookie
            .from(cookieProps.refreshTokenName, refreshToken)
            .httpOnly(true)
            .secure(true)
            .sameSite(cookieProps.sameSiteStrategy)
            .path("/api/v1/auth/refresh")
            .maxAge(jwtProps.refreshExpiry.seconds)
            .build()
}
