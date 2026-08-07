package com.zoonza.pokemoncardshop.catalog.internal.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_card_expansion_id_number",
            columnNames = ["expansion_id", "number"]
        )
    ]
)
class Card private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(nullable = false)
    val expansionId: Long,

    @Column(unique = true, nullable = false)
    val sourceId: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: CardCategory,

    @Column(nullable = false)
    val number: String,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val imageUrl: String,

    @Column(nullable = false)
    val illustrator: String = "Unknown",

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val rarity: CardRarity,

    @Embedded
    val variants: CardVariants,

    @Column(nullable = false)
    val registeredAt: Instant
) {
    companion object {
        fun register(
            expansionId: Long,
            sourceId: String,
            category: CardCategory,
            number: String,
            name: String,
            imageUrl: String,
            illustrator: String,
            rarity: CardRarity,
            variants: CardVariants,
            registeredAt: Instant,
        ): Card = Card(
            expansionId = expansionId,
            sourceId = sourceId,
            category = category,
            number = number,
            name = name,
            imageUrl = imageUrl,
            illustrator = illustrator,
            rarity = rarity,
            variants = variants,
            registeredAt = registeredAt,
        )
    }
}
