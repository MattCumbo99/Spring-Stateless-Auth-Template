package com.mcumbo.template.statelessauthservice.common.interceptor

import com.mcumbo.template.statelessauthservice.common.annotation.RateLimit
import com.mcumbo.template.statelessauthservice.common.service.RateLimitingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.util.concurrent.TimeUnit

@Component
class RateLimitInterceptor(
    private val rateLimitingService: RateLimitingService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler !is HandlerMethod) return true

        // Check if the endpoint has the RateLimit annotation
        val rateLimitAnnotation = handler.getMethodAnnotation(RateLimit::class.java) ?: return true

        val clientIp =
            request
                .getHeader("X-Forwarded-For")
                ?.split(",")
                ?.first()
                ?.trim() ?: request.remoteAddr

        // Scope the bucket to both the plan or endpoint and the client's IP
        val bucketKey = "${rateLimitAnnotation.plan.name}:${request.requestURI}:$clientIp"
        val bucket = rateLimitingService.resolveBucket(bucketKey, rateLimitAnnotation.plan)

        // Attempt to consume 1 token
        val probe = bucket.tryConsumeAndReturnRemaining(1)

        if (probe.isConsumed) {
            return true
        } else {
            // Rejected, return 429 too many requests
            val waitForRefillSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.nanosToWaitForRefill)
            response.addHeader(HttpHeaders.RETRY_AFTER, waitForRefillSeconds.toString())
            response.sendError(
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Too many requests. Please try again later.",
            )

            return false
        }
    }
}
