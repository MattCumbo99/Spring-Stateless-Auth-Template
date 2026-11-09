package com.mcumbo.template.statelessauthservice.common.exception

import com.mcumbo.template.statelessauthservice.common.exception.generic.AppException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import kotlin.time.Clock

@ControllerAdvice
class GlobalExceptionHandler {
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

        return problemDetail
    }
}
