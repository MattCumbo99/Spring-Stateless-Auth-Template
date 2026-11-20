package com.mcumbo.template.statelessauthservice.common.exception

import com.mcumbo.template.statelessauthservice.common.exception.generic.AppException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import kotlin.time.Clock

@ControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ExceptionHandler(AppException::class)
    fun handleAppException(ex: AppException): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(ex.status, ex.message)
        problemDetail.title = ex.status.reasonPhrase
        problemDetail.setProperty("errorCode", ex.errorCode)
        problemDetail.setProperty("timestamp", Clock.System.now())

        return problemDetail
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ProblemDetail {
        val problemDetail =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more fields failed validation checks.",
            )

        problemDetail.title = "Validation Error"

        // Map failing field names to their validation messages
        val fieldErrors =
            ex.bindingResult.fieldErrors.associate { error ->
                error.field to (error.defaultMessage ?: "Invalid value")
            }

        // Attach field errors under RFC 7807 custom properties
        problemDetail.setProperty("errors", fieldErrors)

        problemDetail.setProperty("errorCode", "INVALID_INPUT")

        return problemDetail
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(ex: Exception): ProblemDetail {
        logger.error("Unhandled exception occurred", ex)

        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val problemDetail =
            ProblemDetail.forStatusAndDetail(
                status,
                "An unexpected error occurred",
            )
        problemDetail.title = status.reasonPhrase
        problemDetail.setProperty("errorCode", "INTERNAL_ERROR")
        problemDetail.setProperty("timestamp", Clock.System.now())

        return problemDetail
    }
}
