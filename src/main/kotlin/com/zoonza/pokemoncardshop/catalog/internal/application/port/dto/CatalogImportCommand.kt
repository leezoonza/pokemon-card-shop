package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import java.time.LocalDate

data class CatalogImportCommand(
    val seriesSourceId: String,
    val seriesNameKo: String?,
    val seriesReleaseDate: LocalDate?,
    val expansions: List<ExpansionImportSelection>,
)

data class ExpansionImportSelection(
    val sourceId: String,
    val nameKo: String,
)
