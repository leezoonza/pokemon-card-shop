package com.zoonza.pokemoncardshop.catalog.internal.domain

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
    val registeredAt: Instant
) {
    companion object {
        fun register(
            sourceId: String,
            name: String,
            releaseDate: LocalDate,
            registeredAt: Instant,
        ): Series = Series(
            sourceId = sourceId,
            name = name,
            releaseDate = releaseDate,
            registeredAt = registeredAt,
        )
    }
}
