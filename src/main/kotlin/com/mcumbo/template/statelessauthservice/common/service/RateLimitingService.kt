package com.mcumbo.template.statelessauthservice.common.service

import com.mcumbo.template.statelessauthservice.common.model.RateLimitPlan
import io.github.bucket4j.Bucket
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class RateLimitingService {
    // Key = "planName:clientIdentifier" (e.g., "AUTH_STRICT:192.168.1.1")
    private val buckets = ConcurrentHashMap<String, Bucket>()

    fun resolveBucket(
        key: String,
        plan: RateLimitPlan,
    ): Bucket =
        buckets.computeIfAbsent(key) {
            Bucket.builder().addLimit(plan.toBandwidth()).build()
        }
}
