package com.mcumbo.template.statelessauthservice.authentication.service

import com.mcumbo.template.statelessauthservice.authentication.entity.RefreshToken
import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidSessionException
import com.mcumbo.template.statelessauthservice.authentication.exception.RefreshTokenNotFoundException
import com.mcumbo.template.statelessauthservice.authentication.repository.RefreshTokenRepository
import com.mcumbo.template.statelessauthservice.authentication.types.RefreshTokenStatus.REVOKED
import com.mcumbo.template.statelessauthservice.authentication.types.RefreshTokenStatus.ROTATED
import com.mcumbo.template.statelessauthservice.authentication.util.JwtUtil
import com.mcumbo.template.statelessauthservice.users.entity.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun issueRefreshToken(
        user: User,
        familyId: UUID? = null,
    ): String {
        val refreshToken = jwtService.generateRefreshToken(user)
        val claims = jwtService.parseRefreshClaims(refreshToken)

        val jti = UUID.fromString(claims.id)
        val expiry = claims.expiration.toInstant().atOffset(ZoneOffset.UTC)
        val tokenHash = JwtUtil.hashToken(refreshToken)

        val refreshTokenEntity =
            RefreshToken(
                jti,
                user,
                familyId = familyId ?: UUID.randomUUID(),
                tokenHash,
                expiresAt = expiry,
            )

        refreshTokenRepository.save(refreshTokenEntity)

        return refreshToken
    }

    @Transactional
    fun rotateRefreshToken(token: String) {
        val tokenHash = JwtUtil.hashToken(token)
        val refreshToken =
            refreshTokenRepository.findByTokenHash(tokenHash) ?: throw RefreshTokenNotFoundException()

        preventReusage(refreshToken)
    }

    private fun preventReusage(refreshToken: RefreshToken) {
        if (refreshToken.status == ROTATED) {
            // Attempted to use a rotated token; trigger security incident
            logger.warn(
                "[SECURITY] Refresh token reuse detected. Revoking family chain. userId={}, familyId={}, reusedJti={}",
                refreshToken.user.id,
                refreshToken.familyId,
                refreshToken.jti,
            )

            refreshTokenRepository.revokeFamily(refreshToken.familyId)

            throw InvalidSessionException()
        } else if (refreshToken.status == REVOKED) {
            throw InvalidSessionException()
        }
    }
}
