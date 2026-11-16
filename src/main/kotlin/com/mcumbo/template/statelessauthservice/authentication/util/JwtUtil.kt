package com.mcumbo.template.statelessauthservice.authentication.util

import com.mcumbo.template.statelessauthservice.users.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import java.security.MessageDigest
import java.util.Date
import java.util.HexFormat
import java.util.UUID
import javax.crypto.SecretKey

object JwtUtil {
    fun generateToken(
        user: User,
        expirationMillis: Long,
        key: SecretKey,
        additionalClaims: Map<String, String> = emptyMap(),
    ): String {
        val now = Date()
        val expiry = Date(now.time + expirationMillis)

        val builder =
            Jwts
                .builder()
                .id(UUID.randomUUID().toString())
                .subject(user.id!!.toString())
                .claim("username", user.username)
                .issuedAt(now)
                .expiration(expiry)

        additionalClaims.forEach { (key, value) ->
            builder.claim(key, value)
        }

        return builder.signWith(key).compact()
    }

    fun parseClaims(
        token: String,
        key: SecretKey,
    ): Claims? =
        try {
            Jwts
                .parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (_: JwtException) {
            null
        }

    fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())

        return HexFormat.of().formatHex(hashBytes)
    }
}
