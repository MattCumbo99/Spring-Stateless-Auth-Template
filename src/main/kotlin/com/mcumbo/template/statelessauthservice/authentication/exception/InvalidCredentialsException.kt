package com.mcumbo.template.statelessauthservice.authentication.exception

import com.mcumbo.template.statelessauthservice.common.exception.generic.UnauthorizedException

class InvalidCredentialsException : UnauthorizedException(message = "Invalid credentials.")
