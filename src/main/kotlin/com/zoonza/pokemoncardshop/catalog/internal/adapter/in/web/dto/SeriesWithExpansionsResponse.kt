package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionItem
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesWithExpansions
import java.time.LocalDate

data class SeriesWithExpansionsResponse(
    val seriesId: Long,
    val seriesName: String,
    val expansions: List<ExpansionItemResponse>
) {
    constructor(
        seriesWithExpansions: SeriesWithExpansions
    ) : this(
        seriesId = seriesWithExpansions.seriesId,
        seriesName = seriesWithExpansions.seriesName,
        expansions = seriesWithExpansions.expansions.map { ExpansionItemResponse(it) }
    )
}

data class ExpansionItemResponse(
    val expansionId: Long,
    val name: String,
    val logoUrl: String?,
    val releaseDate: LocalDate,
    val isNew: Boolean
) {
    constructor(
        expansionItem: ExpansionItem
    ) : this(
        expansionId = expansionItem.expansionId,
        name = expansionItem.name,
        logoUrl = expansionItem.logoUrl,
        releaseDate = expansionItem.releaseDate,
        isNew = expansionItem.isNew
    )
}
