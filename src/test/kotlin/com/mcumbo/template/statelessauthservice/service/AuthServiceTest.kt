package com.mcumbo.template.statelessauthservice.service

import com.mcumbo.template.statelessauthservice.authentication.dto.request.LoginRequest
import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidCredentialsException
import com.mcumbo.template.statelessauthservice.authentication.repository.RefreshTokenRepository
import com.mcumbo.template.statelessauthservice.authentication.service.AuthService
import com.mcumbo.template.statelessauthservice.authentication.service.JwtService
import com.mcumbo.template.statelessauthservice.authentication.types.RefreshTokenStatus
import com.mcumbo.template.statelessauthservice.authentication.util.JwtUtil
import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.service.UserService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.equals.shouldNotEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuthServiceTest(
    private val authService: AuthService,
    private val userService: UserService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
) : BaseIntegrationTest() {
    private lateinit var testUser: UserResponse

    @BeforeEach
    fun setup() {
        testUser =
            userService.registerUser(
                RegisterUserRequest(
                    username = "admin",
                    password = "password321",
                ),
            )
    }

    @Nested
    inner class LoginUser {
        @Test
        fun `should authenticate on successful login`() {
            val loginRes =
                authService.loginUser(LoginRequest(username = "admin", password = "password321"))

            val resultData = loginRes.userData
            resultData.username shouldBe "admin"
            resultData.id shouldBe testUser.id

            loginRes.sessionJwt shouldStartWith "eyJ"
            loginRes.refreshJwt shouldStartWith "eyJ"

            loginRes.sessionJwt shouldNotEqual loginRes.refreshJwt

            // Validation check for the token values
            withClue({ "JWT should be parsable." }) {
                jwtService.parseSessionClaims(loginRes.sessionJwt).shouldNotBeNull()
                jwtService.parseRefreshClaims(loginRes.refreshJwt).shouldNotBeNull()
            }

            val refreshHash = JwtUtil.hashToken(loginRes.refreshJwt)
            val loginRefreshToken = refreshTokenRepository.findByTokenHash(refreshHash).shouldNotBeNull()

            loginRefreshToken.user.id shouldBe testUser.id
            loginRefreshToken.status shouldBe RefreshTokenStatus.ACTIVE
            loginRefreshToken.tokenHash shouldNotBe loginRes.refreshJwt

            // Ensure the family ID on the JWT matches that in the database
            val familyJwtId = jwtService.getRefreshFamilyId(loginRes.refreshJwt).shouldNotBeNull()

            withClue({ "Family ID on JWT should match family ID stored in the database." }) {
                familyJwtId shouldBe loginRefreshToken.familyId
            }
        }

        @Test
        fun `should throw invalid credentials exception on any credential discrepancy`() {
            // Valid username, incorrect password
            shouldThrow<InvalidCredentialsException> {
                authService.loginUser(LoginRequest(username = "admin", password = "12345678"))
            }

            // Non-existing user
            shouldThrow<InvalidCredentialsException> {
                authService.loginUser(LoginRequest(username = "admin1", password = "password321"))
            }
        }
    }

    @Nested
    inner class RefreshSession {
        @Test
        fun `should rotate and refresh existing authentication data`() {
            val loginRes =
                authService.loginUser(LoginRequest(username = "admin", password = "password321"))

            val refreshRes = authService.refreshSession(loginRes.refreshJwt)
            refreshRes.refreshJwt shouldStartWith "eyJ"
            refreshRes.sessionJwt shouldStartWith "eyJ"
            refreshRes.refreshJwt shouldNotEqual refreshRes.sessionJwt

            withClue({ "JWT should be parsable." }) {
                jwtService.parseSessionClaims(refreshRes.sessionJwt).shouldNotBeNull()
                jwtService.parseRefreshClaims(refreshRes.refreshJwt).shouldNotBeNull()
            }

            refreshRes.refreshJwt shouldNotEqual loginRes.refreshJwt

            val loginTokenHash = JwtUtil.hashToken(loginRes.refreshJwt)
            val loginRefreshToken =
                refreshTokenRepository.findByTokenHash(loginTokenHash).shouldNotBeNull()

            val refreshTokenHash = JwtUtil.hashToken(refreshRes.refreshJwt)
            val refreshTokenEntity =
                refreshTokenRepository.findByTokenHash(refreshTokenHash).shouldNotBeNull()

            // Ensure the previous refresh token was rotated
            loginRefreshToken.status shouldBe RefreshTokenStatus.ROTATED
            refreshTokenEntity.status shouldBe RefreshTokenStatus.ACTIVE

            withClue({ "New refresh tokens should inherit previous family ID." }) {
                refreshTokenEntity.familyId shouldBe loginRefreshToken.familyId
            }
        }
    }
}
