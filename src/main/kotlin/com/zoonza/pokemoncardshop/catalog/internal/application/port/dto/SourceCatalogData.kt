package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import java.time.LocalDate

data class SourceSeriesSummary(
    val sourceId: String,
    val name: String,
    val logoUrl: String?,
)

data class SourceSeries(
    val sourceId: String,
    val name: String,
    val logoUrl: String?,
    val expansions: List<SourceExpansionSummary>,
)

data class SourceExpansionSummary(
    val sourceId: String,
    val name: String,
    val logoUrl: String?,
    val symbolUrl: String?,
)

data class SourceExpansion(
    val sourceId: String,
    val seriesSourceId: String,
    val name: String,
    val logoUrl: String?,
    val symbolUrl: String?,
    val releaseDate: LocalDate,
    val totalCardCount: Int,
    val officialCardCount: Int,
    val cardSourceIds: List<String>,
)

data class SourceCard(
    val sourceId: String,
    val expansionSourceId: String,
    val category: String,
    val number: String,
    val name: String,
    val imageUrl: String?,
    val illustrator: String?,
    val rarity: String,
    val variants: SourceCardVariants,
    val abilities: List<SourceAbility> = emptyList(),
    val dexIds: List<Int> = emptyList(),
    val hp: Int? = null,
    val types: List<String> = emptyList(),
    val evolveFrom: String? = null,
    val description: String? = null,
    val stage: String? = null,
    val suffix: String? = null,
    val attacks: List<SourceAttack> = emptyList(),
    val weaknesses: List<SourceWeakRes> = emptyList(),
    val resistances: List<SourceWeakRes> = emptyList(),
    val retreat: Int? = null,
    val effect: String? = null,
    val trainerType: String? = null,
    val energyType: String? = null,
)

data class SourceCardVariants(
    val firstEdition: Boolean,
    val holo: Boolean,
    val normal: Boolean,
    val reverse: Boolean,
    val wPromo: Boolean,
)

data class SourceAbility(
    val type: String,
    val name: String,
    val effect: String,
)

data class SourceAttack(
    val name: String,
    val cost: List<String>,
    val effect: String?,
    val damage: String?,
)

data class SourceWeakRes(
    val type: String,
    val value: String?,
)
