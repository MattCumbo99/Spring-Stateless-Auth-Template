package com.mcumbo.template.statelessauthservice.common.logging

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.UUID

@Component
class RequestLoggingInterceptor : HandlerInterceptor {
    private val requestIdAttribute = "requestId"
    private val startTimeAttribute = "startTime"

    private val logger = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (!logger.isDebugEnabled) return true

        request.setAttribute(startTimeAttribute, System.currentTimeMillis())

        val requestId = UUID.randomUUID().toString().substring(0, 8)
        request.setAttribute(requestIdAttribute, requestId)

        logger.debug("-> [REQUEST:  {}] {}", requestId, request.asInfoLog())

        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        val isUnhandledError = HttpStatus.valueOf(response.status).is5xxServerError

        if (ex != null && isUnhandledError) {
            logger.error("Unhandled exception occurred", ex)
        }

        if (!logger.isDebugEnabled) return

        val start = request.getAttribute(startTimeAttribute) as Long
        val duration = System.currentTimeMillis() - start

        val requestId = request.getAttribute(requestIdAttribute) as String

        val loggedResponse = response.asInfoLog(request, duration)

        logger.debug("<- [RESPONSE: {}] {}", requestId, loggedResponse)
    }
}
