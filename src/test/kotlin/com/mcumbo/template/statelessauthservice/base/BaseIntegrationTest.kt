package com.mcumbo.template.statelessauthservice.base

import com.mcumbo.template.statelessauthservice.authentication.properties.CookieProps
import com.mcumbo.template.statelessauthservice.common.config.SecurityConfig
import com.mcumbo.template.statelessauthservice.common.config.WebConfig
import com.mcumbo.template.statelessauthservice.config.TestContainersConfiguration
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.client.RestTestClient
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestContainersConfiguration::class, SecurityConfig::class, WebConfig::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
abstract class BaseIntegrationTest {
    @Autowired protected lateinit var httpClient: RestTestClient

    @Autowired private lateinit var cookieProps: CookieProps

    @AfterEach
    fun cleanup() {
        unmockkAll()
    }
}
