package com.mcumbo.template.statelessauthservice.base

import com.mcumbo.template.statelessauthservice.authentication.dto.request.LoginRequest
import com.mcumbo.template.statelessauthservice.authentication.properties.CookieProps
import com.mcumbo.template.statelessauthservice.common.config.SecurityConfig
import com.mcumbo.template.statelessauthservice.common.config.WebConfig
import com.mcumbo.template.statelessauthservice.config.TestContainersConfiguration
import com.mcumbo.template.statelessauthservice.users.repository.UserRepository
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Import
import org.springframework.http.ResponseCookie
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration::class, SecurityConfig::class, WebConfig::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestPropertySource(properties = ["app.rate-limit.enabled=false"])
abstract class BaseIntegrationTest {
    @Autowired protected lateinit var httpClient: RestTestClient

    @Autowired private lateinit var userRepository: UserRepository

    @Autowired private lateinit var cookieProps: CookieProps

    @Autowired private lateinit var cacheManager: CacheManager

    @AfterEach
    fun cleanup() {
        userRepository.deleteAllInBatch()
        cacheManager.resetCaches()
        unmockkAll()
    }

    data class AuthCookies(
        val sessionCookie: ResponseCookie,
        val refreshCookie: ResponseCookie,
    )

    protected fun authCookiesFor(
        username: String,
        password: String,
    ): AuthCookies {
        val loginRes =
            httpClient
                .post()
                .uri("/auth/login")
                .body(LoginRequest(username, password))
                .exchange()
                .returnResult()

        val sessionCookie =
            loginRes.responseCookies[cookieProps.sessionTokenName]?.firstOrNull()
                ?: throw IllegalStateException("Failed to acquire session cookie for $username")

        val refreshCookie =
            loginRes.responseCookies[cookieProps.refreshTokenName]?.firstOrNull()
                ?: throw IllegalStateException("Failed to acquire refresh cookie for $username")

        return AuthCookies(sessionCookie, refreshCookie)
    }

    protected fun AuthCookies.toMultiMap(): MultiValueMap<String, String> {
        val cookieMap = LinkedMultiValueMap<String, String>()
        cookieMap.add(this.sessionCookie.name, this.sessionCookie.value)
        cookieMap.add(this.refreshCookie.name, this.refreshCookie.value)

        return cookieMap
    }
}
