package com.zoonza.pokemoncardshop.catalog.internal.domain

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

    @Column(unique = true, nullable = false)
    val name: String,

    @Embedded
    val count: CardCount,

    @Embedded
    val image: ExpansionImage,

    @Column(nullable = false)
    val releaseDate: LocalDate,

    @Column(nullable = false)
    val registeredAt: Instant
) {
    companion object {
        fun register(
            seriesId: Long,
            sourceId: String,
            name: String,
            count: CardCount,
            image: ExpansionImage,
            releaseDate: LocalDate,
            registeredAt: Instant,
        ): Expansion = Expansion(
            seriesId = seriesId,
            sourceId = sourceId,
            name = name,
            count = count,
            image = image,
            releaseDate = releaseDate,
            registeredAt = registeredAt,
        )
    }
}
