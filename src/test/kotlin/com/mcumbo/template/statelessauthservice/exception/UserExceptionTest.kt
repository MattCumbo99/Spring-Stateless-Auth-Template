package com.mcumbo.template.statelessauthservice.exception

import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.users.exception.UserAlreadyExistsException
import com.mcumbo.template.statelessauthservice.users.exception.UsernameNotFoundException
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class UserExceptionTest : BaseIntegrationTest() {
    @Test
    fun `UserAlreadyExistsException should be 409 conflict`() {
        val exception = UserAlreadyExistsException()

        exception.status shouldBe HttpStatus.CONFLICT
    }

    @Test
    fun `UsernameNotFoundException should be 404 not found`() {
        val exception = UsernameNotFoundException("")

        exception.status shouldBe HttpStatus.NOT_FOUND
    }
}
