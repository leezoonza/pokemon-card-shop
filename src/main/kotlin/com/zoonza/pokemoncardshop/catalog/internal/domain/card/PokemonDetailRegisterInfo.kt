package com.zoonza.pokemoncardshop.catalog.internal.domain.card

data class PokemonDetailRegisterInfo(
    val dexIds: Set<Int> = emptySet(),
    val hp: Int? = null,
    val types: Set<String> = emptySet(),
    val evolveFrom: String? = null,
    val description: String? = null,
    val stage: String? = null,
    val suffix: String? = null,
    val attacks: List<Attack> = emptyList(),
    val weaknesses: List<WeakRes> = emptyList(),
    val resistances: List<WeakRes> = emptyList(),
    val retreat: Int? = null,
)
