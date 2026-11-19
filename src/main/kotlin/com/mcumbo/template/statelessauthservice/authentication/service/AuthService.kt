package com.mcumbo.template.statelessauthservice.authentication.service

import com.mcumbo.template.statelessauthservice.authentication.dto.request.LoginRequest
import com.mcumbo.template.statelessauthservice.authentication.dto.response.AuthResult
import com.mcumbo.template.statelessauthservice.authentication.dto.response.RefreshResult
import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidCredentialsException
import com.mcumbo.template.statelessauthservice.authentication.exception.InvalidSessionException
import com.mcumbo.template.statelessauthservice.authentication.provider.CustomUserDetails
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.dto.response.toResponse
import com.mcumbo.template.statelessauthservice.users.service.UserService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userService: UserService,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val tokenRevokerService: TokenRevokerService,
    private val passwordEncoder: PasswordEncoder,
) {
    /**
     * Attempts to authenticate the [loginRequest] credentials. If the credentials match against an
     * existing user, a new refresh token is created to track the session.
     *
     * @throws InvalidCredentialsException If any part of the [loginRequest] could not be verified.
     */
    fun loginUser(loginRequest: LoginRequest): AuthResult {
        val user =
            userService.getUserEntityByUsername(loginRequest.username.lowercase())
                ?: throw InvalidCredentialsException()

        val passwordMismatch = !passwordEncoder.matches(loginRequest.password, user.passwordHash)
        if (passwordMismatch) {
            throw InvalidCredentialsException()
        }

        // Begin providing authentication details for the user
        val sessionJwt = jwtService.generateSessionJwt(user)
        val refreshJwt = refreshTokenService.issueRefreshToken(user, familyId = null)

        return AuthResult(
            user.toResponse(),
            sessionJwt,
            refreshJwt,
        )
    }

    /**
     * Retrieves current user data stored in the [SecurityContextHolder], or `null` if the user is not
     * authenticated.
     */
    fun getCurrentUser(): UserResponse? {
        val auth = SecurityContextHolder.getContext().authentication
        val isValidSession = auth != null && auth.isAuthenticated && auth.principal is CustomUserDetails

        return if (isValidSession) {
            // Get the username from the auth context
            val username = (auth.principal as CustomUserDetails).username

            userService.getUserByUsername(username)
        } else {
            null
        }
    }

    /**
     * Rotates the stored refresh token and returns new authentication data.
     *
     * @throws InvalidSessionException If details from the [refreshJwt] could not be parsed.
     */
    fun refreshSession(refreshJwt: String): RefreshResult {
        // Treat grabbing any token data as a malformed session
        val familyId = jwtService.getRefreshFamilyId(refreshJwt) ?: throw InvalidSessionException()
        val username = jwtService.getRefreshUsername(refreshJwt) ?: throw InvalidSessionException()
        val user = userService.getUserEntityByUsername(username) ?: throw InvalidSessionException()

        // Rotate the existing refresh token
        refreshTokenService.rotateToken(refreshJwt)

        // Issue new token
        val newRefreshJwt = refreshTokenService.issueRefreshToken(user, familyId)

        return RefreshResult(
            sessionJwt = jwtService.generateSessionJwt(user),
            refreshJwt = newRefreshJwt,
        )
    }

    fun logoutUser(refreshJwt: String?) {
        if (refreshJwt != null) {
            // Leniently revoke tokens in the family
            jwtService.getRefreshFamilyId(refreshJwt)?.let { familyId ->
                tokenRevokerService.revokeFamily(familyId)
            }
        }
    }
}
