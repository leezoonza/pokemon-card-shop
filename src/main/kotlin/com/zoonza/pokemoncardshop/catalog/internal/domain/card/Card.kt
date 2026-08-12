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
                abilities = info.abilities.toMutableList(),
                pokemonDetail = createPokemonDetail(info),
                trainerDetail = createTrainerDetail(info),
                energyDetail = createEnergyDetail(info),
                registeredAt = info.registeredAt,
                updatedAt = info.registeredAt,
            )
        }

        private fun createPokemonDetail(info: CardRegisterInfo): PokemonDetail? {
            if (info.category != CardCategory.POKEMON) return null

            return info.pokemonDetail?.let(PokemonDetail::register)
        }

        private fun createTrainerDetail(info: CardRegisterInfo): TrainerDetail? {
            if (info.category != CardCategory.TRAINER) return null

            return info.trainerDetail?.let(TrainerDetail::register)
        }

        private fun createEnergyDetail(info: CardRegisterInfo): EnergyDetail? {
            if (info.category != CardCategory.ENERGY) return null

            return info.energyDetail?.let(EnergyDetail::register)
        }
    }
}
