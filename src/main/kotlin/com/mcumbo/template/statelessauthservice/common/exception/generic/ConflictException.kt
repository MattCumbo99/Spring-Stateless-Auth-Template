package com.mcumbo.template.statelessauthservice.common.exception.generic

import org.springframework.http.HttpStatus

/** Generic 409 Conflict exception to be extended by specific domain entity exceptions. */
open class ConflictException(
    message: String,
    cause: Throwable? = null,
) : AppException(
        message,
        status = HttpStatus.CONFLICT,
        errorCode = "CONFLICT",
        cause,
    )
