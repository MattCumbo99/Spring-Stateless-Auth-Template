package com.mcumbo.template.statelessauthservice.exception

import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidCredentialsException
import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidSessionException
import com.mcumbo.template.statelessauthservice.authentication.exception.RefreshTokenNotFoundException
import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class AuthExceptionTest : BaseIntegrationTest() {
    @Test
    fun `InvalidCredentialsException should be 401 unauthorized`() {
        val exception = InvalidCredentialsException()

        exception.status shouldBe HttpStatus.UNAUTHORIZED
    }

    @Test
    fun `InvalidSessionException should be 401 unauthorized`() {
        val exception = InvalidSessionException()

        exception.status shouldBe HttpStatus.UNAUTHORIZED
    }

    @Test
    fun `RefreshTokenNotFoundException should be 404 not found`() {
        val exception = RefreshTokenNotFoundException()

        exception.status shouldBe HttpStatus.NOT_FOUND
    }
}
