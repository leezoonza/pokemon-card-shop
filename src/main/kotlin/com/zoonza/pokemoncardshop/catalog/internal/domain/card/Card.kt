package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
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

    @Column(nullable = false)
    val imageUrl: String,

    @Column(nullable = false)
    val illustrator: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val rarity: CardRarity,

    @Embedded
    val variants: CardVariants,

    @ElementCollection
    @CollectionTable(
        name = "card_abilities",
        joinColumns = [JoinColumn(name = "card_id")],
    )
    val abilities: MutableList<Ability> = mutableListOf(),

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "pokemon_detail_id", unique = true)
    val pokemonDetail: PokemonDetail?,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "trainer_detail_id", unique = true)
    val trainerDetail: TrainerDetail?,

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "energy_detail_id", unique = true)
    val energyDetail: EnergyDetail?,

    @Column(nullable = false)
    val registeredAt: Instant,

    @Column(nullable = false)
    val updatedAt: Instant,
) {
//    companion object {
//        fun register(
//            expansionId: Long,
//            sourceId: String,
//            category: CardCategory,
//            localId: String,
//            name: Name,
//            imageUrl: String,
//            illustrator: String?,
//            rarity: CardRarity,
//            variants: CardVariants,
//            registeredAt: Instant,
//        ): Card {
//            if (name.en.isBlank()) {
//                throw DomainException(CardErrorCode.ENGLISH_NAME_REQUIRED)
//            }
//
//            return Card(
//                expansionId = expansionId,
//                sourceId = sourceId,
//                category = category,
//                localId = localId,
//                name = name,
//                imageUrl = imageUrl,
//                illustrator = illustrator ?: "Unknown",
//                rarity = rarity,
//                variants = variants,
//                registeredAt = registeredAt,
//                updatedAt = registeredAt
//            )
//        }
//    }
}
