package com.mcumbo.template.statelessauthservice.ratelimit

import com.mcumbo.template.statelessauthservice.authentication.dto.request.LoginRequest
import com.mcumbo.template.statelessauthservice.base.BaseRateLimitTest
import com.mcumbo.template.statelessauthservice.common.model.RateLimitPlan
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class AuthRateLimitTest : BaseRateLimitTest() {
    private val authLimit = RateLimitPlan.AUTH_STRICT.capacity.toInt()

    @Test
    fun `should rate limit login`() {
        val loginUri = "/auth/login"
        val request =
            LoginRequest(
                username = "admin",
                password = "password",
            )

        repeat(authLimit) {
            httpClient
                .post()
                .uri(loginUri)
                .body(request)
                .exchange()
                .expectStatus()
                .isUnauthorized
        }

        httpClient
            .post()
            .uri(loginUri)
            .body(request)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value())
    }
}
