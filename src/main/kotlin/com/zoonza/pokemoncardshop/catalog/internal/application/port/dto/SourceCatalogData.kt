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
    val category: String,
    val localId: String,
    val name: String,
    val imageUrl: String?,
    val illustrator: String?,
    val rarity: String,
    val variants: SourceCardVariants,
)

data class SourceCardVariants(
    val firstEdition: Boolean,
    val holo: Boolean,
    val normal: Boolean,
    val reverse: Boolean,
    val wPromo: Boolean,
)
