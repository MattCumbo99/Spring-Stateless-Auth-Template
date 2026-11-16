package com.mcumbo.template.statelessauthservice.authentication.entity

import com.mcumbo.template.statelessauthservice.authentication.types.RefreshTokenStatus
import com.mcumbo.template.statelessauthservice.users.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Id @Column(nullable = false) var jti: UUID,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,
    @Column(name = "family_id", nullable = false) var familyId: UUID,
    @Column(name = "token_hash", nullable = false) var tokenHash: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: RefreshTokenStatus = RefreshTokenStatus.ACTIVE,
    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "expires_at", nullable = false) var expiresAt: OffsetDateTime,
)
