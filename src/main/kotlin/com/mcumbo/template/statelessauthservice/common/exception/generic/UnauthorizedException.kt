package com.mcumbo.template.statelessauthservice.common.exception.generic

import org.springframework.http.HttpStatus

/** Generic 401 Unauthorized exception to be extended by specific domain entity exceptions. */
open class UnauthorizedException(
    message: String,
    cause: Throwable? = null,
) : AppException(
        message,
        status = HttpStatus.UNAUTHORIZED,
        errorCode = "UNAUTHORIZED",
        cause,
    )
