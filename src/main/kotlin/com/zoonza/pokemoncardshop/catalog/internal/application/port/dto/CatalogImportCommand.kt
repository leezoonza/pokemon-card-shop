package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import java.time.LocalDate

data class CatalogImportCommand(
    val seriesSourceId: String,
    val seriesReleaseDate: LocalDate,
    val expansionSourceIds: List<String>,
)
