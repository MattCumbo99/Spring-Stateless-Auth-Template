package com.mcumbo.template.statelessauthservice.users.service

import com.mcumbo.template.statelessauthservice.common.properties.CacheNames
import com.mcumbo.template.statelessauthservice.users.dto.request.RegisterUserRequest
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.dto.response.toResponse
import com.mcumbo.template.statelessauthservice.users.entity.User
import com.mcumbo.template.statelessauthservice.users.exception.UserAlreadyExistsException
import com.mcumbo.template.statelessauthservice.users.exception.UsernameNotFoundException
import com.mcumbo.template.statelessauthservice.users.repository.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepo: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    /**
     * Retrieves user information matching the provided username, caching the result under
     * [CacheNames.USERS] keyed by [username].
     *
     * @throws UsernameNotFoundException If no user exists with this username.
     */
    @Cacheable(CacheNames.USERS)
    fun getUserByUsername(username: String): UserResponse {
        val user = userRepo.findByUsername(username) ?: throw UsernameNotFoundException(username)

        return user.toResponse()
    }

    fun getUserEntityByUsername(username: String): User? = userRepo.findByUsername(username)

    /**
     * Creates and saves a new user based on the provided [registerRequest] information.
     *
     * @throws UserAlreadyExistsException If the given username already exists under a different user.
     */
    fun registerUser(registerRequest: RegisterUserRequest): UserResponse {
        if (userRepo.existsByUsername(registerRequest.username)) {
            throw UserAlreadyExistsException()
        }

        val newUser =
            User(
                username = registerRequest.username,
                passwordHash = passwordEncoder.encode(registerRequest.password)!!,
            )

        userRepo.save(newUser)

        return newUser.toResponse()
    }
}
