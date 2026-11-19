package com.mcumbo.template.statelessauthservice.scheduler

import com.mcumbo.template.statelessauthservice.authentication.repository.RefreshTokenRepository
import com.mcumbo.template.statelessauthservice.authentication.scheduler.RefreshTokenCleanupScheduler
import com.mcumbo.template.statelessauthservice.authentication.service.RefreshTokenService
import com.mcumbo.template.statelessauthservice.authentication.types.RefreshTokenStatus
import com.mcumbo.template.statelessauthservice.authentication.util.JwtUtil
import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.service.UserService
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class RefreshTokenCleanupSchedulerTest(
    private val refreshTokenCleanupScheduler: RefreshTokenCleanupScheduler,
    private val refreshTokenService: RefreshTokenService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userService: UserService,
) : BaseIntegrationTest() {
    @Test
    fun `should delete expired tokens`() {
        val userData =
            userService.registerUser(
                RegisterUserRequest(
                    username = "admin",
                    password = "password",
                ),
            )
        val testUser = userService.getUserEntityByUsername(userData.username).shouldNotBeNull()

        val genFamilyId = UUID.randomUUID()

        val activeJwt = refreshTokenService.issueRefreshToken(testUser, familyId = genFamilyId)
        val rotatedJwt = refreshTokenService.issueRefreshToken(testUser, familyId = genFamilyId)
        val revokedJwt = refreshTokenService.issueRefreshToken(testUser, familyId = genFamilyId)

        // Unrelated family ID active and expired
        val activeDiffJwt =
            refreshTokenService.issueRefreshToken(testUser, familyId = UUID.randomUUID())

        val activeToken = refreshTokenRepository.findByTokenHash(activeJwt.hashed()).shouldNotBeNull()
        val activeDiffToken =
            refreshTokenRepository.findByTokenHash(activeDiffJwt.hashed()).shouldNotBeNull()
        activeToken.status shouldBe RefreshTokenStatus.ACTIVE
        activeDiffToken.status shouldBe RefreshTokenStatus.ACTIVE

        val rotatedToken = refreshTokenRepository.findByTokenHash(rotatedJwt.hashed()).shouldNotBeNull()
        rotatedToken.status = RefreshTokenStatus.ROTATED

        val revokedToken = refreshTokenRepository.findByTokenHash(revokedJwt.hashed()).shouldNotBeNull()
        revokedToken.status = RefreshTokenStatus.REVOKED

        val now = OffsetDateTime.now()

        // Modify the other tokens to be expired
        activeDiffToken.expiresAt = now.minusDays(1)
        revokedToken.expiresAt = now.minusHours(1)
        rotatedToken.expiresAt = now.minusMinutes(1)

        activeToken.expiresAt = now.plusMinutes(1) // Ensure this token is still active

        // Write to DB
        refreshTokenRepository.saveAll(listOf(activeToken, activeDiffToken, rotatedToken, revokedToken))

        // Run the scheduler job
        refreshTokenCleanupScheduler.runRefreshTokenCleanup()

        val refreshTokens = refreshTokenRepository.findAll().toList().shouldHaveSize(1)
        refreshTokens.first().jti shouldBe activeToken.jti
    }

    private fun String.hashed() = JwtUtil.hashToken(this)
}
