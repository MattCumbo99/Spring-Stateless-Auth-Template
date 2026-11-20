package com.mcumbo.template.statelessauthservice.common.exception.generic

import org.springframework.http.HttpStatus

/** Generic 404 Not Found exception to be extended by specific domain entity exceptions. */
open class NotFoundException(
    message: String,
    errorCode: String = "NOT_FOUND",
    cause: Throwable? = null,
) : AppException(
        message,
        status = HttpStatus.NOT_FOUND,
        errorCode,
        cause,
    )
