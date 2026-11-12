package com.mcumbo.template.statelessauthservice.controller

import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.service.UserService
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.client.expectBody

class UserControllerTest(
    private val userService: UserService,
) : BaseIntegrationTest() {
    @Nested
    inner class RegisterUser {
        private val registerUri = "/users/register"

        @Test
        fun `should return 201 created on successful registration`() {
            val request = RegisterUserRequest(username = "test_user", password = "password123")

            val responseBody = httpClient
                .post()
                .uri(registerUri)
                .body(request)
                .exchange()
                .expectStatus()
                .isCreated
                .expectBody<UserResponse>()
                .returnResult()
                .responseBody
                .shouldNotBeNull()

            responseBody.username shouldBe "test_user"
        }

        @Test
        fun `should return 400 bad request on invalid request parameters`() {
            val invalidRequests =
                listOf(
                    RegisterUserRequest(username = "test user", password = "password123"),
                    RegisterUserRequest(username = "SUPERMAN", password = "password123"),
                    RegisterUserRequest(username = "", password = "password123"),
                    RegisterUserRequest(username = "good_username", password = ""),
                    RegisterUserRequest(
                        username = "superduperlongkindofusernameshouldnotbegood",
                        password = "password123",
                    ),
                    RegisterUserRequest(username = "%d2#)(*E@19191", password = "password123"),
                    RegisterUserRequest(username = "monkeyman", password = "password123".repeat(10)),
                    RegisterUserRequest(username = "chud13", password = "short"),
                )

            invalidRequests.forEach { request ->
                httpClient
                    .post()
                    .uri(registerUri)
                    .body(request)
                    .exchange()
                    .expectStatus()
                    .isBadRequest
            }
        }

        @Test
        fun `should return 409 conflict on registration attempt with existing username`() {
            userService.registerUser(
                RegisterUserRequest(username = "test_user", password = "password123"),
            )

            // Same username, different password
            val request = RegisterUserRequest(username = "test_user", password = "traveller123")

            httpClient
                .post()
                .uri(registerUri)
                .body(request)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.CONFLICT.value())
        }
    }

    @Nested
    inner class GetUser {
        private val getUserUri = { username: String -> "/users/$username" }

        @Test
        fun `should return 200 OK upon retrieving user data`() {
            // Register an existing user
            userService.registerUser(
                RegisterUserRequest(username = "test_user", password = "password123"),
            )

            val responseBody = httpClient
                .get()
                .uri(getUserUri("test_user"))
                .exchange()
                .expectStatus()
                .isOk
                .expectBody<UserResponse>()
                .returnResult()
                .responseBody
                .shouldNotBeNull()

            responseBody.username shouldBe "test_user"
        }

        @Test
        fun `should return 404 not found when retrieving a non-existing user`() {
            httpClient
                .get()
                .uri(getUserUri("non_existing"))
                .exchange()
                .expectStatus()
                .isNotFound
        }
    }
}
