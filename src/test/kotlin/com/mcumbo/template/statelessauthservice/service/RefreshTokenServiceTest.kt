package com.mcumbo.template.statelessauthservice.service

import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidSessionException
import com.mcumbo.template.statelessauthservice.authentication.repository.RefreshTokenRepository
import com.mcumbo.template.statelessauthservice.authentication.service.RefreshTokenService
import com.mcumbo.template.statelessauthservice.authentication.types.RefreshTokenStatus
import com.mcumbo.template.statelessauthservice.authentication.util.JwtUtil
import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.entity.User
import com.mcumbo.template.statelessauthservice.users.service.UserService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.UUID

class RefreshTokenServiceTest(
    private val refreshTokenService: RefreshTokenService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userService: UserService,
) : BaseIntegrationTest() {
    private lateinit var testUser: User

    @BeforeEach
    fun setup() {
        val userData =
            userService.registerUser(
                RegisterUserRequest(
                    username = "admin",
                    password = "password",
                ),
            )

        testUser = userService.getUserEntityByUsername(userData.username)!!
    }

    @Test
    fun `should create and issue a refresh token`() {
        val testFamilyId = UUID.randomUUID()
        val refreshJwt = refreshTokenService.issueRefreshToken(testUser, testFamilyId)

        refreshJwt shouldStartWith "eyJ"

        // Check that the entity was saved
        val refreshEntity =
            refreshTokenRepository.findByTokenHash(refreshJwt.hashed()).shouldNotBeNull()
        refreshEntity.status shouldBe RefreshTokenStatus.ACTIVE
        refreshEntity.familyId shouldBe testFamilyId
        refreshEntity.user.id shouldBe testUser.id
        refreshEntity.tokenHash shouldNotBe refreshJwt
    }

    @Test
    fun `should rotate token`() {
        val oldRefreshJwt = refreshTokenService.issueRefreshToken(testUser, familyId = null)
        val oldRefreshToken =
            refreshTokenRepository.findByTokenHash(oldRefreshJwt.hashed()).shouldNotBeNull()

        oldRefreshToken.status shouldBe RefreshTokenStatus.ACTIVE

        // Rotate the current token
        refreshTokenService.rotateToken(oldRefreshJwt)

        val newRefreshToken =
            refreshTokenRepository.findByTokenHash(oldRefreshJwt.hashed()).shouldNotBeNull()
        newRefreshToken.status shouldBe RefreshTokenStatus.ROTATED
    }

    @Test
    fun `should revoke family and throw exception when attempting to rotate a rotated token`() {
        val genFamilyId = UUID.randomUUID()

        // First token is rotated
        val refreshJwtRotated = refreshTokenService.issueRefreshToken(testUser, familyId = genFamilyId)
        val refreshTokenRotated =
            refreshTokenRepository.findByTokenHash(refreshJwtRotated.hashed()).shouldNotBeNull()

        // Rotate it
        refreshTokenRotated.status = RefreshTokenStatus.ROTATED
        refreshTokenRepository.save(refreshTokenRotated)

        // Second token is active
        val refreshJwtActive = refreshTokenService.issueRefreshToken(testUser, familyId = genFamilyId)
        val refreshTokenActive =
            refreshTokenRepository.findByTokenHash(refreshJwtActive.hashed()).shouldNotBeNull()

        refreshTokenActive.status shouldBe RefreshTokenStatus.ACTIVE

        // Create a third token with a different family ID
        refreshTokenService.issueRefreshToken(testUser, familyId = UUID.randomUUID())

        // Attempt to rotate the rotated token
        shouldThrow<InvalidSessionException> {
            refreshTokenService.rotateToken(refreshJwtRotated)
        }

        val refreshTokens = refreshTokenRepository.findAll().toList().shouldHaveSize(3)

        refreshTokens.forAll { refreshToken ->
            if (refreshToken.familyId == genFamilyId) {
                withClue({ "Every token under the same family should be revoked" }) {
                    refreshToken.status shouldBe RefreshTokenStatus.REVOKED
                }
            } else {
                withClue({ "Refresh token under different family IDs should not be affected" }) {
                    refreshToken.status shouldNotBe RefreshTokenStatus.REVOKED
                }
            }
        }
    }

    @Test
    fun `should delete expired tokens based on time`() {
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

        val deletedCount = refreshTokenService.deleteAllExpiredSince(now)
        withClue({ "Deleting tokens should return the amount of entities deleted." }) {
            deletedCount shouldBe 3
        }

        val refreshTokens = refreshTokenRepository.findAll().toList().shouldHaveSize(1)
        refreshTokens.first().jti shouldBe activeToken.jti
    }

    private fun String.hashed() = JwtUtil.hashToken(this)
}
