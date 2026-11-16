package com.mcumbo.template.statelessauthservice.common.exception.generic

import org.springframework.http.HttpStatus

/** Generic 422 Unprocessable Content exception to be extended by specific domain exceptions. */
open class UnprocessableContentException(
    message: String,
    cause: Throwable? = null,
) : AppException(
        message,
        status = HttpStatus.UNPROCESSABLE_CONTENT,
        errorCode = "UNPROCESSABLE_CONTENT",
        cause,
    )
