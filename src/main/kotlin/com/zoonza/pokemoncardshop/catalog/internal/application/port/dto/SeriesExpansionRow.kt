package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import java.time.LocalDate

data class SeriesExpansionRow(
    val seriesId: Long,
    val seriesName: String,
    val expansionId: Long,
    val expansionName: String,
    val expansionLogoUrl: String?,
    val expansionReleaseDate: LocalDate,
    val isNew: Boolean
)
