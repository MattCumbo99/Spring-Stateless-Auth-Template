package com.mcumbo.template.statelessauthservice.base

import com.mcumbo.template.statelessauthservice.common.config.SecurityConfig
import com.mcumbo.template.statelessauthservice.common.config.WebConfig
import com.mcumbo.template.statelessauthservice.users.repository.UserRepository
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
import org.testcontainers.utility.TestcontainersConfiguration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration::class, SecurityConfig::class, WebConfig::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
abstract class BaseIntegrationTest {
    @Autowired protected lateinit var httpClient: RestTestClient

    @Autowired private lateinit var userRepository: UserRepository

    @AfterEach
    fun cleanup() {
        userRepository.deleteAllInBatch()
        unmockkAll()
    }
}
