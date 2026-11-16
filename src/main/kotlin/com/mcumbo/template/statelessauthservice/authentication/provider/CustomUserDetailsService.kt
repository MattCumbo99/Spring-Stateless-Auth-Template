package com.mcumbo.template.statelessauthservice.authentication.provider

import com.mcumbo.template.statelessauthservice.users.exception.UsernameNotFoundException
import com.mcumbo.template.statelessauthservice.users.service.UserService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userService: UserService,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userService.getUserEntityByUsername(username) ?: throw UsernameNotFoundException(username)

        return CustomUserDetails(user)
    }
}
