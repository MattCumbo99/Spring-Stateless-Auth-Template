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
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenRevokerService: TokenRevokerService,
    private val jwtService: JwtService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * Creates and saves a new refresh token.
     *
     * @param user Owner of the new token.
     * @param familyId Family ID to pass onto the new refresh token. A `null` value will create a new
     *   ID.
     * @return The created refresh token's JWT.
     */
    fun issueRefreshToken(
        user: User,
        familyId: UUID? = null,
    ): String {
        val genFamilyId = familyId ?: UUID.randomUUID()
        val refreshToken = jwtService.generateRefreshJwt(user, genFamilyId)
        val claims = jwtService.parseRefreshClaims(refreshToken)!!

        val jti = UUID.fromString(claims.id)
        val expiry = claims.expiration.toInstant().atOffset(ZoneOffset.UTC)
        val tokenHash = JwtUtil.hashToken(refreshToken)

        val refreshTokenEntity =
            RefreshToken(
                jti,
                user,
                familyId = genFamilyId,
                tokenHash,
                expiresAt = expiry,
            )

        refreshTokenRepository.save(refreshTokenEntity)

        return refreshToken
    }

    /**
     * Sets the status of the [RefreshToken] owned under [refreshJwt] to [ROTATED].
     *
     * If the token status is already [ROTATED], a security incident is logged and all existing
     * refresh tokens using the same family ID are set to [REVOKED].
     *
     * @throws RefreshTokenNotFoundException If no refresh token matches [refreshJwt].
     * @throws InvalidSessionException If the token status is either [ROTATED] or [REVOKED].
     */
    @Transactional
    fun rotateToken(refreshJwt: String) {
        val tokenHash = JwtUtil.hashToken(refreshJwt)
        val refreshToken =
            refreshTokenRepository.findByTokenHash(tokenHash) ?: throw RefreshTokenNotFoundException()

        preventReusage(refreshToken)

        refreshToken.status = ROTATED
    }

    @Transactional
    fun deleteAllExpiredSince(time: OffsetDateTime): Int = refreshTokenRepository.deleteAllExpiredSince(time)

    private fun preventReusage(refreshToken: RefreshToken) {
        if (refreshToken.status == ROTATED) {
            // Attempted to use a rotated token; trigger security incident
            logger.warn(
                "Refresh token reuse detected. Revoking family chain. userId={}, familyId={}, reusedJti={}",
                refreshToken.user.id,
                refreshToken.familyId,
                refreshToken.jti,
            )

            tokenRevokerService.revokeFamily(refreshToken.familyId)

            throw InvalidSessionException()
        } else if (refreshToken.status == REVOKED) {
            throw InvalidSessionException()
        }
    }
}
