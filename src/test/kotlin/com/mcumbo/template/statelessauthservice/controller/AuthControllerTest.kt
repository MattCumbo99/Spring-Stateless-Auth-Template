package com.mcumbo.template.statelessauthservice.controller

import com.mcumbo.template.statelessauthservice.authentication.dto.request.LoginRequest
import com.mcumbo.template.statelessauthservice.authentication.properties.CookieProps
import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.service.UserService
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeBlank
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.client.ExchangeResult
import org.springframework.test.web.servlet.client.expectBody
import kotlin.collections.orEmpty

class AuthControllerTest(
    private val userService: UserService,
    private val cookieProps: CookieProps,
) : BaseIntegrationTest() {
    private lateinit var testUser: UserResponse

    private val testUserPassword = "password123"

    @BeforeEach
    fun init() {
        testUser =
            userService.registerUser(
                RegisterUserRequest(
                    username = "test_user",
                    password = testUserPassword,
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
                    username = testUser.username,
                    password = testUserPassword,
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
        fun `should return 400 bad request on any invalid request field`() {
            val invalidRequests =
                listOf(
                    LoginRequest(
                        username = "", // Blank username
                        password = testUserPassword,
                    ),
                    LoginRequest(
                        username = testUser.username,
                        password = "", // Blank password
                    ),
                )

            invalidRequests.forEach { request ->
                httpClient
                    .post()
                    .uri(loginUri)
                    .body(request)
                    .exchange()
                    .expectStatus()
                    .isBadRequest
            }
        }

        @Test
        fun `should return 401 unauthorized on any credential mis-match`() {
            val invalidRequests =
                listOf(
                    LoginRequest(
                        username = testUser.username, // Existing user, incorrect password
                        password = "password1234",
                    ),
                    LoginRequest(
                        username = "test_user123", // Non-existing user
                        password = testUserPassword,
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

        @Test
        fun `should return 403 forbidden when authenticated`() {
            val authCookies = authCookiesFor(testUser.username, testUserPassword)

            httpClient
                .post()
                .uri(loginUri)
                .body(LoginRequest(testUser.username, testUserPassword))
                .cookies {
                    it.addAll(authCookies.toMultiMap())
                }.exchange()
                .expectStatus()
                .isForbidden
        }
    }

    @Nested
    inner class RefreshSession {
        private val refreshUri = "/auth/refresh"

        @Test
        fun `should refresh and renew authentication cookies`() {
            val authCookies = authCookiesFor(testUser.username, testUserPassword).toMultiMap()

            val response =
                httpClient
                    .post()
                    .uri(refreshUri)
                    .cookies { it.addAll(authCookies) }
                    .exchange()
                    .expectStatus()
                    .isOk
                    .returnResult()

            val newSessionCookies = response.responseCookies[cookieProps.sessionTokenName].orEmpty()
            val newRefreshCookies = response.responseCookies[cookieProps.refreshTokenName].orEmpty()

            newSessionCookies shouldHaveSize 1
            newRefreshCookies shouldHaveSize 1

            // Check session
            val newSessionJwt = newSessionCookies.first().value.shouldNotBeBlank()!!
            val oldSessionJwt = authCookies[cookieProps.sessionTokenName]?.first()!!
            newSessionJwt shouldStartWith "eyJ"
            newSessionJwt shouldNotBeEqual oldSessionJwt

            // Check refresh
            val newRefreshJwt = newRefreshCookies.first().value.shouldNotBeBlank()!!
            val oldRefreshJwt = authCookies[cookieProps.refreshTokenName]?.first()!!
            newRefreshJwt shouldStartWith "eyJ"
            newRefreshJwt shouldNotBeEqual oldRefreshJwt
        }
    }

    @Nested
    inner class GetMe {
        private val myUri = "/auth/me"

        @Test
        fun `should get current authenticated user information`() {
            val authCookies = authCookiesFor(testUser.username, testUserPassword).toMultiMap()

            val responseBody =
                httpClient
                    .get()
                    .uri(myUri)
                    .cookies {
                        it.addAll(authCookies)
                    }.exchange()
                    .expectStatus()
                    .isOk
                    .expectBody<UserResponse>()
                    .returnResult()
                    .responseBody
                    .shouldNotBeNull()

            responseBody.username shouldBe testUser.username
        }
    }

    @Nested
    inner class Logout {
        private val logoutUri = "/auth/logout"

        @Test
        fun `should send blank auth cookies to clear session`() {
            val authCookies = authCookiesFor(testUser.username, testUserPassword).toMultiMap()

            httpClient
                .post()
                .uri(logoutUri)
                .cookies { it.addAll(authCookies) }
                .exchange()
                .expectStatus()
                .isNoContent
                .returnResult()
                .shouldClearAuth()
        }

        @Test
        fun `should clear auth cookies regardless of authentication status`() {
            httpClient
                .post()
                .uri(logoutUri)
                .exchange()
                .expectStatus()
                .isNoContent
                .returnResult()
                .shouldClearAuth()
        }

        private fun ExchangeResult.shouldClearAuth() {
            val sessionCookies = this.responseCookies[cookieProps.sessionTokenName].orEmpty()
            sessionCookies shouldHaveSize 1

            val sessionCookie = sessionCookies.first()
            sessionCookie.maxAge.isZero shouldBe true
            sessionCookie.value.shouldBeBlank()

            val refreshCookies = this.responseCookies[cookieProps.refreshTokenName].orEmpty()
            refreshCookies shouldHaveSize 1

            val refreshCookie = refreshCookies.first()
            refreshCookie.maxAge.isZero shouldBe true
            refreshCookie.value.shouldBeBlank()
        }
    }
}
