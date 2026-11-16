package com.mcumbo.template.statelessauthservice.cache

import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.common.properties.CacheNames
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.repository.UserRepository
import com.mcumbo.template.statelessauthservice.users.service.UserService
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.cache.CacheManager

class UserCacheTest(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val cacheManager: CacheManager,
) : BaseIntegrationTest() {
    private lateinit var testUser: UserResponse

    @BeforeEach
    fun setUp() {
        cacheManager.getCache(CacheNames.USERS)?.clear()
        userRepository.deleteAllInBatch()

        testUser =
            userService.registerUser(
                RegisterUserRequest(
                    username = "tester",
                    password = "password",
                ),
            )
    }

    @Test
    fun `getUserByPublicId should return cached data`() {
        // First call fetches from database and caches the result
        val firstResult = userService.getUserByUsername(testUser.username)
        firstResult.username shouldBe testUser.username

        // Change the entity directly (bypasses cache)
        val entity = userRepository.findByUsername(testUser.username).shouldNotBeNull()
        entity.username = "tester2day"
        userRepository.save(entity)

        // Second call should return the cached result, proving the database did not get touched
        val cachedResult = userService.getUserByUsername(testUser.username)
        cachedResult.username shouldBe testUser.username
    }
}
