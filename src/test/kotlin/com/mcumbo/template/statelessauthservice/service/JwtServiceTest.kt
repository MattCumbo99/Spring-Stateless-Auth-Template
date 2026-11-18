package com.mcumbo.template.statelessauthservice.service

import com.mcumbo.template.statelessauthservice.authentication.service.JwtService
import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.service.UserService
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtServiceTest(
    private val jwtService: JwtService,
    private val userService: UserService,
) : BaseIntegrationTest() {
    private lateinit var testUserData: UserResponse

    @BeforeEach
    fun setup() {
        testUserData =
            userService.registerUser(
                RegisterUserRequest(
                    username = "admin",
                    password = "racecar",
                ),
            )
    }

    @Test
    fun `should generate valid session token`() {
        val testUser = userService.getUserEntityByUsername(testUserData.username).shouldNotBeNull()
        val genToken = jwtService.generateSessionToken(testUser)

        val claims = jwtService.parseSessionClaims(genToken).shouldNotBeNull()
        claims.subject shouldBe testUser.id.toString()

        val claimsUsername = claims["username"].shouldNotBeNull().shouldBeInstanceOf<String>()
        claimsUsername shouldBe testUser.username
    }

    @Test
    fun `should generate valid refresh token`() {
        val testUser = userService.getUserEntityByUsername(testUserData.username).shouldNotBeNull()

        val familyId = UUID.randomUUID()
        val genToken = jwtService.generateRefreshToken(testUser, familyId)

        val claims = jwtService.parseRefreshClaims(genToken).shouldNotBeNull()

        val claimFamilyId = claims["family_id"].shouldNotBeNull().shouldBeInstanceOf<String>()
        claimFamilyId shouldBe familyId.toString()

        claims.subject shouldBe testUser.id.toString()

        val claimsUsername = claims["username"].shouldNotBeNull().shouldBeInstanceOf<String>()
        claimsUsername shouldBe testUser.username
    }
}
