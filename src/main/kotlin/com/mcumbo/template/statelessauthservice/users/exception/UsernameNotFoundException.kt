package com.mcumbo.template.statelessauthservice.users.exception

import com.mcumbo.template.statelessauthservice.common.exception.generic.NotFoundException

class UsernameNotFoundException(
    username: String,
) : NotFoundException(
        message = "User '$username' not found.",
    )
