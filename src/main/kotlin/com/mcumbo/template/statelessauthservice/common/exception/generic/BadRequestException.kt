package com.mcumbo.template.statelessauthservice.common.exception.generic

import org.springframework.http.HttpStatus

/**
 * Generic 400 Bad Request exception to be extended by specific domain exceptions, or thrown
 * directly for client-side errors.
 */
open class BadRequestException(
    message: String,
    cause: Throwable? = null,
) : AppException(
        message,
        status = HttpStatus.BAD_REQUEST,
        errorCode = "BAD_REQUEST",
        cause,
    )
