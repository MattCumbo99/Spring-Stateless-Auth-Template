package com.mcumbo.template.statelessauthservice.common.annotation

import com.mcumbo.template.statelessauthservice.common.model.RateLimitPlan

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit(
    val plan: RateLimitPlan = RateLimitPlan.STANDARD_API,
)
