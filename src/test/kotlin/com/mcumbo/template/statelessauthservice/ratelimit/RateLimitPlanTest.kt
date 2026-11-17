package com.mcumbo.template.statelessauthservice.ratelimit

import com.mcumbo.template.statelessauthservice.base.BaseIntegrationTest
import com.mcumbo.template.statelessauthservice.common.model.RateLimitPlan
import com.mcumbo.template.statelessauthservice.common.model.RateLimitStrategy
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

class RateLimitPlanTest : BaseIntegrationTest() {
    @Test
    fun `auth strict plan should have correct strict capacity and interval refill`() {
        val plan = RateLimitPlan.AUTH_STRICT

        plan.capacity shouldBe 5
        plan.refillTokens shouldBe 5
        plan.refillDuration shouldBe 5.minutes.toJavaDuration()
        plan.strategy shouldBe RateLimitStrategy.INTERVAL
    }

    @Test
    fun `standard api plan should have correct capacity and greedy refill`() {
        val plan = RateLimitPlan.STANDARD_API

        plan.capacity shouldBe 100
        plan.refillTokens shouldBe 100
        plan.refillDuration shouldBe 1.minutes.toJavaDuration()
        plan.strategy shouldBe RateLimitStrategy.GREEDY
    }
}
