package com.mcumbo.template.statelessauthservice.controller

import com.mcumbo.template.statelessauthservice.authentication.dto.request.LoginRequest
import com.mcumbo.template.statelessauthservice.authentication.properties.CookieProps
import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.service.UserService
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.client.expectBody

class AuthControllerTest(
    private val userService: UserService,
    private val cookieProps: CookieProps,
) : BaseIntegrationTest() {
    private lateinit var testUser: UserResponse

    @BeforeEach
    fun init() {
        testUser =
            userService.registerUser(
                RegisterUserRequest(
                    username = "test_user",
                    password = "password123",
                ),
            )
    }

    @Nested
    inner class Login {
        private val loginUri = "/auth/login"

        @Test
        fun `should return 200 OK with authentication cookies`() {
            val request =
                LoginRequest(
                    username = "test_user",
                    password = "password123",
                )

            val response =
                httpClient
                    .post()
                    .uri(loginUri)
                    .body(request)
                    .exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<UserResponse>()
                    .returnResult()

            val responseBody = response.responseBody.shouldNotBeNull()
            responseBody.username shouldBe testUser.username

            // Ensure cookies were attached:
            // Session cookie verification
            val sessionCookies = response.responseCookies[cookieProps.sessionTokenName].orEmpty()
            sessionCookies shouldHaveSize 1

            val sessionJwt = sessionCookies.first().value.shouldNotBeBlank()!!
            sessionJwt shouldStartWith "eyJ" // Standard base64url header for JSON Web Tokens

            // Refresh cookie verification
            val refreshCookies = response.responseCookies[cookieProps.refreshTokenName].orEmpty()
            refreshCookies shouldHaveSize 1

            val refreshJwt = refreshCookies.first().value.shouldNotBeBlank()!!
            refreshJwt shouldStartWith "eyJ" // Standard base64url header for JSON Web Tokens
        }

        @Test
        fun `should return 401 unauthorized on any credential mis-match`() {
            val invalidRequests =
                listOf(
                    LoginRequest(
                        username = "test_user", // Existing user, incorrect password
                        password = "password1234",
                    ),
                    LoginRequest(
                        username = "test_user123", // Non-existing user
                        password = "password123",
                    ),
                )

            invalidRequests.forEach { request ->
                httpClient
                    .post()
                    .uri(loginUri)
                    .body(request)
                    .exchange()
                    .expectStatus()
                    .isUnauthorized
            }
        }
    }
}
