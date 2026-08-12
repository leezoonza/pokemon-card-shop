package com.zoonza.pokemoncardshop.catalog.internal.domain.expansion

import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.common.error.DomainException
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
class Expansion private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val seriesId: Long,

    @Column(unique = true, nullable = false)
    val sourceId: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "en",
            column = Column(unique = true, nullable = false),
        ),
        AttributeOverride(
            name = "ko",
            column = Column(unique = true, nullable = false),
        )
    )
    val name: Name,

    @Embedded
    val count: CardCount,

    @Embedded
    val image: ExpansionImage,

    @Column(nullable = false)
    val releaseDate: LocalDate,

    @Column(nullable = false)
    val registeredAt: Instant,

    @Column(nullable = false)
    val updatedAt: Instant,
) {
    companion object {
        fun register(info: ExpansionRegisterInfo): Expansion {
            if (info.name.en.isBlank()) {
                throw DomainException(ExpansionErrorCode.ENGLISH_NAME_REQUIRED)
            }

            if (info.name.ko.isNullOrBlank()) {
                throw DomainException(ExpansionErrorCode.KOREAN_NAME_REQUIRED)
            }

            return Expansion(
                seriesId = info.seriesId,
                sourceId = info.sourceId,
                name = info.name,
                count = info.count,
                image = info.image,
                releaseDate = info.releaseDate,
                registeredAt = info.registeredAt,
                updatedAt = info.registeredAt,
            )
        }
    }
}
