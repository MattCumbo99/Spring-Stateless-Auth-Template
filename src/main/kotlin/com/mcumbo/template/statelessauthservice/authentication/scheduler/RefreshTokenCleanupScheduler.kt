package com.mcumbo.template.statelessauthservice.authentication.scheduler

import com.mcumbo.template.statelessauthservice.authentication.service.RefreshTokenService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Component
class RefreshTokenCleanupScheduler(
    private val refreshTokenService: RefreshTokenService,
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(cron = $$"${app.scheduling.refresh-token-cleanup-cron}")
    @Transactional
    fun runRefreshTokenCleanup() {
        val startTime = System.currentTimeMillis()

        logger.info("Started scheduled purge of expired refresh tokens.")

        val totalDeleted = refreshTokenService.deleteAllExpiredSince(OffsetDateTime.now())
        val duration = System.currentTimeMillis() - startTime

        logger.info(
            "Finished cleanup. Deleted $totalDeleted records in $duration ms.",
        )
    }
}
