package com.mcumbo.template.statelessauthservice.service

import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.exception.UserAlreadyExistsException
import com.mcumbo.template.statelessauthservice.users.exception.UsernameNotFoundException
import com.mcumbo.template.statelessauthservice.users.repository.UserRepository
import com.mcumbo.template.statelessauthservice.users.service.UserService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class UserServiceTest(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : BaseIntegrationTest() {
    @Nested
    inner class CreateUser {
        @Test
        fun `should create and save a new user`() {
            val result =
                userService.registerUser(
                    RegisterUserRequest(
                        username = "admin",
                        password = "password",
                    ),
                )

            val user = userRepository.findByUsername(result.username).shouldNotBeNull()

            user.username shouldBe "admin"
            user.passwordHash shouldNotBe "password"

            withClue({ "Password hash should match value on creation" }) {
                val matches = passwordEncoder.matches("password", user.passwordHash)

                matches.shouldBeTrue()
            }
        }

        @Test
        fun `should throw user already exists exception on duplicate username`() {
            // Register the first user
            userService.registerUser(
                RegisterUserRequest(
                    username = "person",
                    password = "goodpass",
                ),
            )

            shouldThrow<UserAlreadyExistsException> {
                userService.registerUser(
                    RegisterUserRequest(
                        username = "person",
                        password = "someotherpassword",
                    ),
                )
            }
        }
    }

    @Nested
    inner class GetUserByUsername {
        @Test
        fun `should get user by username`() {
            // Register an existing user
            userService.registerUser(
                RegisterUserRequest(
                    username = "admin",
                    password = "password",
                ),
            )

            val result = userService.getUserByUsername("admin")

            result.username shouldBe "admin"
        }

        @Test
        fun `should throw username not found exception on non-existing user`() {
            shouldThrow<UsernameNotFoundException> {
                userService.getUserByUsername("admin")
            }
        }
    }
}
