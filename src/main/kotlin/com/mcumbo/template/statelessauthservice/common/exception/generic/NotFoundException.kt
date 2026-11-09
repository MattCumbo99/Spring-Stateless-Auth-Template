package com.mcumbo.template.statelessauthservice.common.exception.generic

import org.springframework.http.HttpStatus

/** Generic 404 Not Found exception to be extended by specific domain entity exceptions. */
open class NotFoundException(
    message: String,
    cause: Throwable? = null,
) : AppException(
        message,
        status = HttpStatus.NOT_FOUND,
        errorCode = "NOT_FOUND",
        cause,
    )
