package com.mcumbo.template.statelessauthservice.common.interceptor

import com.mcumbo.template.statelessauthservice.common.annotation.NoRateLimit
import com.mcumbo.template.statelessauthservice.common.annotation.RateLimit
import com.mcumbo.template.statelessauthservice.common.model.RateLimitPlan
import com.mcumbo.template.statelessauthservice.common.service.RateLimitingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.util.concurrent.TimeUnit

@Component
@ConditionalOnProperty(
    prefix = "app.rate-limit",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class RateLimitInterceptor(
    private val rateLimitingService: RateLimitingService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (handler !is HandlerMethod || request.method.equals("OPTIONS", ignoreCase = true)) {
            return true
        }

        // Check for explicit bypass
        if (handler.hasMethodAnnotation(NoRateLimit::class.java)) {
            return true
        }

        // Check for method override, or use the base default plan
        val plan =
            handler.getMethodAnnotation(RateLimit::class.java)?.plan ?: RateLimitPlan.STANDARD_API

        // Resolve bucket and consume token
        val clientIp =
            request
                .getHeader("X-Forwarded-For")
                ?.split(",")
                ?.first()
                ?.trim() ?: request.remoteAddr

        // Scope the bucket to both the plan or endpoint and the client's IP
        val bucketKey = buildBucketKey(plan, request.requestURI, clientIp)
        val bucket = rateLimitingService.resolveBucket(bucketKey, plan)

        // Attempt to consume 1 token
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            return true
        }

        val waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.nanosToWaitForRefill).coerceAtLeast(1)
        response.addHeader(HttpHeaders.RETRY_AFTER, waitSeconds.toString())
        response.sendError(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Too many requests. Please wait a moment and try again.",
        )

        return false
    }

    private fun buildBucketKey(
        plan: RateLimitPlan,
        requestUri: String,
        clientIp: String,
    ): String = "${plan.name}:$requestUri:$clientIp"
}
