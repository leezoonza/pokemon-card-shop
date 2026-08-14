package com.zoonza.pokemoncardshop.catalog.internal.domain.series

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

    @Column(unique = true, nullable = false)
    val name: String,

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
            name: String,
            releaseDate: LocalDate,
            registeredAt: Instant
        ): Series {
            if (name.isBlank()) {
                throw DomainException(SeriesErrorCode.NAME_REQUIRED)
            }

            return Series(
                sourceId = sourceId,
                name = name,
                releaseDate = releaseDate,
                registeredAt = registeredAt,
                updatedAt = registeredAt,
            )
        }
    }
}
