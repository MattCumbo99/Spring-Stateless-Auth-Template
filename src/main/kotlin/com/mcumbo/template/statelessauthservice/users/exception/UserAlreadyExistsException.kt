package com.mcumbo.template.statelessauthservice.users.exception

import com.mcumbo.template.statelessauthservice.common.exception.generic.ConflictException

class UserAlreadyExistsException : ConflictException(message = "User already exists.")
