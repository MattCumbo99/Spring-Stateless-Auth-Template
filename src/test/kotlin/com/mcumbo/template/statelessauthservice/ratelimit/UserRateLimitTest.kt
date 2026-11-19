package com.mcumbo.template.statelessauthservice.ratelimit

import com.mcumbo.template.statelessauthservice.base.BaseRateLimitTest
import com.mcumbo.template.statelessauthservice.common.model.RateLimitPlan
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import io.kotest.matchers.equals.shouldNotEqual
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class UserRateLimitTest : BaseRateLimitTest() {
    private val authLimit = RateLimitPlan.AUTH_STRICT.capacity.toInt()

    @Test
    fun `should rate limit user registration`() {
        val registerUri = "/users/register"
        val request =
            RegisterUserRequest(
                username = "", // Blank username to ensure a new user does not get created
                password = "s1mplepass",
            )

        repeat(authLimit) {
            httpClient
                .post()
                .uri(registerUri)
                .body(request)
                .exchange()
                .returnResult()
                .status
                .shouldNotEqual(HttpStatus.TOO_MANY_REQUESTS)
        }

        httpClient
            .post()
            .uri(registerUri)
            .body(request)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value())
    }
}
