package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import java.time.Instant

data class CardRegisterInfo(
    val expansionId: Long,
    val sourceId: String,
    val localId: String,
    val name: Name,
    val category: CardCategory,
    val imageUrl: String,
    val illustrator: String?,
    val rarity: CardRarity,
    val variants: CardVariants,
    val abilities: List<Ability> = emptyList(),
    val pokemonDetail: PokemonDetailRegisterInfo? = null,
    val trainerDetail: TrainerDetailRegisterInfo? = null,
    val energyDetail: EnergyDetailRegisterInfo? = null,
    val registeredAt: Instant,
)
