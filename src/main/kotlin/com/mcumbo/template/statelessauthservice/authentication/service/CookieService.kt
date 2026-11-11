package com.mcumbo.template.statelessauthservice.authentication.service

import com.mcumbo.template.statelessauthservice.authentication.properties.CookieProps
import com.mcumbo.template.statelessauthservice.authentication.properties.JwtProps
import com.mcumbo.template.statelessauthservice.users.entity.User
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CookieService(
    private val cookieProps: CookieProps,
    private val jwtProps: JwtProps,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
) {
    fun createSessionCookie(user: User): ResponseCookie {
        val sessionToken = jwtService.generateSessionToken(user)

        return buildSessionCookie(sessionToken)
    }

    fun createRefreshCookie(
        user: User,
        familyId: UUID? = null,
    ): ResponseCookie {
        val refreshToken = refreshTokenService.issueRefreshToken(user, familyId)

        return buildRefreshCookie(refreshToken)
    }

    fun clearAuthCookies(response: HttpServletResponse) {
        val blankSessionCookie =
            ResponseCookie
                .from(cookieProps.sessionTokenName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite(cookieProps.sameSiteStrategy)
                .path("/")
                .maxAge(0)
                .build()

        val blankRefreshCookie =
            ResponseCookie
                .from(cookieProps.refreshTokenName, "")
                .httpOnly(true)
                .secure(true)
                .sameSite(cookieProps.sameSiteStrategy)
                .path("/api/v1/auth/refresh")
                .maxAge(0)
                .build()

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
