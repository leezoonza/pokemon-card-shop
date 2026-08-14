package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import java.time.LocalDate

data class SeriesWithExpansions(
    val seriesId: Long,
    val seriesName: String,
    val expansions: List<ExpansionItem>
)

data class ExpansionItem(
    val expansionId: Long,
    val name: String,
    val logoUrl: String?,
    val releaseDate: LocalDate,
    val isNew: Boolean
)
