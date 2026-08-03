package com.zoonza.pokemoncardshop.auth.internal.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(uniqueConstraints = [
    UniqueConstraint(
        name = "uk_external_identity_provider_subject",
        columnNames = ["provider", "subject"]
    ),
    UniqueConstraint(
        name = "uk_external_identity_provider_member_id",
        columnNames = ["provider", "member_id"]
    )
])
class ExternalIdentity private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val provider: IdentityProvider,

    @Column(nullable = false)
    val subject: String,

    @Column(nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    val createdAt: Instant,

    @Column(nullable = false)
    var lastAuthenticatedAt: Instant
) {
    companion object {
        fun register(
            provider: IdentityProvider,
            subject: String,
            memberId: Long,
            createdAt: Instant,
        ): ExternalIdentity =
            ExternalIdentity(
                provider = provider,
                subject = subject,
                memberId = memberId,
                createdAt = createdAt,
                lastAuthenticatedAt = createdAt
            )
    }
}