package com.mcumbo.template.statelessauthservice.authentication.exception

import com.mcumbo.template.statelessauthservice.common.exception.generic.NotFoundException

class RefreshTokenNotFoundException : NotFoundException(message = "Refresh token not found.")
