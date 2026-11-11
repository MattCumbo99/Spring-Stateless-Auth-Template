package com.mcumbo.template.statelessauthservice.authentication.service

import com.mcumbo.template.statelessauthservice.authentication.properties.JwtProps
import com.mcumbo.template.statelessauthservice.authentication.util.JwtUtil
import com.mcumbo.template.statelessauthservice.users.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.UUID

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

    fun generateRefreshToken(
        user: User,
        familyId: UUID? = null,
    ): String =
        JwtUtil.generateToken(
            user,
            expirationMillis = jwtProps.refreshExpiry.toMillis(),
            key = refreshKey,
            additionalClaims =
                mapOf("family_id" to (familyId?.toString() ?: UUID.randomUUID().toString())),
        )

    fun getSessionUsername(sessionJwt: String): String? = parseSessionClaims(sessionJwt)?.get("username", String::class.java)

    fun getRefreshUsername(refreshJwt: String): String? = parseRefreshClaims(refreshJwt)?.get("username", String::class.java)

    fun getRefreshFamilyId(refreshJwt: String): UUID? {
        val claims = parseSessionClaims(refreshJwt) ?: return null
        val strFamilyId = claims.get("family_id", String::class.java) ?: return null

        return UUID.fromString(strFamilyId)
    }

    fun parseRefreshClaims(token: String): Claims? = JwtUtil.parseClaims(token, refreshKey)

    fun parseSessionClaims(token: String): Claims? = JwtUtil.parseClaims(token, sessionKey)
}
