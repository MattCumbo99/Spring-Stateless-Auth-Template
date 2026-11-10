package com.mcumbo.template.statelessauthservice.authentication.service

import com.mcumbo.template.statelessauthservice.authentication.properties.JwtProps
import com.mcumbo.template.statelessauthservice.authentication.util.JwtUtil
import com.mcumbo.template.statelessauthservice.users.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service

@Service
class JwtService(
    private val jwtProps: JwtProps,
) {
    private val sessionKey = Keys.hmacShaKeyFor(jwtProps.sessionSecret.toByteArray())
    private val refreshKey = Keys.hmacShaKeyFor(jwtProps.refreshSecret.toByteArray())

    fun generateSessionToken(user: User): String =
        JwtUtil.generateToken(
            user,
            expirationMillis = jwtProps.sessionExpiry.toMillis(),
            key = sessionKey,
        )

    fun generateRefreshToken(user: User): String =
        JwtUtil.generateToken(
            user,
            expirationMillis = jwtProps.refreshExpiry.toMillis(),
            key = refreshKey,
        )

    fun parseRefreshClaims(token: String): Claims = JwtUtil.parseClaims(token, refreshKey)

    fun parseSessionClaims(token: String): Claims = JwtUtil.parseClaims(token, sessionKey)
}
