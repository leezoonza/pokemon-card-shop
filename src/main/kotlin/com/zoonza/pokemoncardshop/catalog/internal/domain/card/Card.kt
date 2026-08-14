package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.common.error.DomainException
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_card_expansion_id_local_id",
            columnNames = ["expansion_id", "local_id"]
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
    val localId: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "en",
            column = Column(nullable = false),
        ),
        AttributeOverride(
            name = "ko",
            column = Column(nullable = true),
        )
    )
    val name: Name,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: CardCategory,

    @Column
    val imageUrl: String?,

    @Column(nullable = false)
    val illustrator: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val rarity: CardRarity,

    @Embedded
    val variants: CardVariants,

    @Column(nullable = false)
    val registeredAt: Instant,

    @Column(nullable = false)
    val updatedAt: Instant,
) {
    companion object {
        fun register(info: CardRegisterInfo): Card {
            if (info.name.en.isBlank()) {
                throw DomainException(CardErrorCode.ENGLISH_NAME_REQUIRED)
            }

            return Card(
                expansionId = info.expansionId,
                sourceId = info.sourceId,
                category = info.category,
                localId = info.localId,
                name = info.name,
                imageUrl = info.imageUrl,
                illustrator = info.illustrator ?: "Unknown",
                rarity = info.rarity,
                variants = info.variants,
                registeredAt = info.registeredAt,
                updatedAt = info.registeredAt,
            )
        }
    }
}
