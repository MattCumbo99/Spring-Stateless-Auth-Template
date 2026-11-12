package com.mcumbo.template.statelessauthservice.authentication.controller

import com.mcumbo.template.statelessauthservice.authentication.dto.request.LoginRequest
import com.mcumbo.template.statelessauthservice.authentication.service.AuthService
import com.mcumbo.template.statelessauthservice.users.dto.response.UserResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/login")
    fun login(
        @RequestBody @Valid loginRequest: LoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): UserResponse = authService.loginUser(loginRequest, request, response)

    @GetMapping("/me")
    fun getCurrentUser(): UserResponse = authService.getCurrentUser()

    @PostMapping("/refresh")
    fun refreshSession(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) = authService.refreshSession(request, response)

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) = authService.logoutUser(request, response)
}
