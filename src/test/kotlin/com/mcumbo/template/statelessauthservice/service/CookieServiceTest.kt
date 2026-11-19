package com.mcumbo.template.statelessauthservice.service

import com.mcumbo.template.statelessauthservice.authentication.properties.JwtProps
import com.mcumbo.template.statelessauthservice.authentication.service.CookieService
import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CookieServiceTest(
    private val cookieService: CookieService,
    private val jwtProps: JwtProps,
) : BaseIntegrationTest() {
    @Test
    fun `should build secure session cookie`() {
        val sessionCookie = cookieService.createSessionCookie("sample-jwt")

        sessionCookie.isSecure.shouldBeTrue()
        sessionCookie.isHttpOnly.shouldBeTrue()
        sessionCookie.value shouldBe "sample-jwt"
        sessionCookie.path shouldBe "/"
        sessionCookie.maxAge shouldBe jwtProps.sessionExpiry
    }

    @Test
    fun `should build secure refresh cookie`() {
        val refreshCookie = cookieService.createRefreshCookie("sample-jwt")

        refreshCookie.isSecure.shouldBeTrue()
        refreshCookie.isHttpOnly.shouldBeTrue()
        refreshCookie.value shouldBe "sample-jwt"
        refreshCookie.path shouldBe "/api/v1/auth/refresh"
        refreshCookie.maxAge shouldBe jwtProps.refreshExpiry
    }
}
