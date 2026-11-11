package com.mcumbo.template.statelessauthservice.users.controller

import com.mcumbo.template.statelessauthservice.common.annotation.AnonymousEndpoint
import com.mcumbo.template.statelessauthservice.common.annotation.PublicEndpoint
import com.mcumbo.template.statelessauthservice.users.dto.request.CreateUserRequest
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import com.mcumbo.template.statelessauthservice.users.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/{username}")
    @PublicEndpoint
    fun getUserByUsername(
        @PathVariable username: String,
    ): UserResponse = userService.getUserByUsername(username)

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @AnonymousEndpoint
    fun registerUser(
        @Valid @RequestBody registerRequest: CreateUserRequest,
    ): UserResponse = userService.createUser(registerRequest)
}
