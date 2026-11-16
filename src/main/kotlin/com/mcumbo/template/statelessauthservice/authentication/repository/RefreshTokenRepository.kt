package com.mcumbo.template.statelessauthservice.authentication.repository

import com.mcumbo.template.statelessauthservice.authentication.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.OffsetDateTime
import java.util.UUID

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenHash(hash: String): RefreshToken?

    @Modifying
    @Query(
        """
            UPDATE RefreshToken t
            SET t.status = 'REVOKED'
            WHERE t.familyId = :familyId
        """,
    )
    fun revokeFamily(familyId: UUID)

    @Modifying
    @Query(
        """
            DELETE FROM RefreshToken t WHERE t.expiresAt < :time
        """,
    )
    fun deleteAllExpiredSince(time: OffsetDateTime): Int
}
