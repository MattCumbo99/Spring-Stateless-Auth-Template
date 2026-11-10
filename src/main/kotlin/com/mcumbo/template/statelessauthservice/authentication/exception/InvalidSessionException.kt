package com.mcumbo.template.statelessauthservice.authentication.exception

import com.mcumbo.template.statelessauthservice.common.exception.generic.UnauthorizedException

class InvalidSessionException : UnauthorizedException(message = "Invalid or expired session.")
