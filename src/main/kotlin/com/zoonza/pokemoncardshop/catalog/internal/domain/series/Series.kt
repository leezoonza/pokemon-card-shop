package com.zoonza.pokemoncardshop.catalog.internal.domain.series

import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.common.error.DomainException
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate

@Entity
class Series private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

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

    @Column(nullable = false)
    val releaseDate: LocalDate,

    @Column(nullable = false)
    val registeredAt: Instant,

    @Column(nullable = false)
    val updatedAt: Instant,
) {
    companion object {
        fun register(
            sourceId: String,
            name: Name,
            releaseDate: LocalDate,
            registeredAt: Instant,
        ): Series {
            if (name.en.isBlank()) {
                throw DomainException(SeriesErrorCode.ENGLISH_NAME_REQUIRED)
            }

            if (name.ko.isNullOrBlank()) {
                throw DomainException(SeriesErrorCode.KOREAN_NAME_REQUIRED)
            }

            return Series(
                sourceId = sourceId,
                name = name,
                releaseDate = releaseDate,
                registeredAt = registeredAt,
                updatedAt = registeredAt
            )
        }
    }
}
