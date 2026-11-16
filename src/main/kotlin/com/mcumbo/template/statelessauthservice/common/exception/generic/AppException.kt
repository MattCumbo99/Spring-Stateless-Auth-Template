package com.mcumbo.template.statelessauthservice.common.exception.generic

import org.springframework.http.HttpStatus

abstract class AppException(
    override val message: String,
    val status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    val errorCode: String = "INTERNAL_SERVER_ERROR",
    cause: Throwable? = null,
) : RuntimeException(message, cause)
