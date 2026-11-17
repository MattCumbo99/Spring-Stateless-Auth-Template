package com.mcumbo.template.statelessauthservice.common.model

import io.github.bucket4j.Bandwidth
import java.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

enum class RateLimitPlan(
    val capacity: Long,
    val refillTokens: Long,
    val refillDuration: Duration,
    val strategy: RateLimitStrategy,
) {
    /**
     * Strict authentication plan.
     * - Capacity: 5
     * - Refill Tokens: 5
     * - Refill Duration: 5 minutes
     * - Strategy: Interval
     */
    AUTH_STRICT(
        capacity = 5,
        refillTokens = 5,
        refillDuration = 5.minutes.toJavaDuration(),
        strategy = RateLimitStrategy.INTERVAL,
    ),

    /**
     * Base rate limiter plan.
     * - Capacity: 100
     * - Refill Tokens: 100
     * - Refill Duration: 1 minute
     * - Strategy: Greedy
     */
    STANDARD_API(
        capacity = 100,
        refillTokens = 100,
        refillDuration = 1.minutes.toJavaDuration(),
        strategy = RateLimitStrategy.GREEDY,
    ),
    ;

    fun toBandwidth(): Bandwidth {
        val builder = Bandwidth.builder().capacity(capacity)

        return when (strategy) {
            RateLimitStrategy.GREEDY -> builder.refillGreedy(refillTokens, refillDuration).build()
            RateLimitStrategy.INTERVAL -> builder.refillIntervally(refillTokens, refillDuration).build()
        }
    }
}
