package com.zoonza.pokemoncardshop.auth.internal.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_external_account_provider_subject",
            columnNames = ["provider", "subject"]
        ),
        UniqueConstraint(
            name = "uk_external_account_provider_member_id",
            columnNames = ["provider", "member_id"]
        )
    ]
)
class ExternalAccount private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val provider: ExternalAccountProvider,

    @Column(nullable = false)
    val subject: String,

    @Column(nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    val linkedAt: Instant
) {
    companion object {
        fun register(
            provider: ExternalAccountProvider,
            subject: String,
            memberId: Long,
            linkedAt: Instant,
        ): ExternalAccount =
            ExternalAccount(
                provider = provider,
                subject = subject,
                memberId = memberId,
                linkedAt = linkedAt
            )
    }
}