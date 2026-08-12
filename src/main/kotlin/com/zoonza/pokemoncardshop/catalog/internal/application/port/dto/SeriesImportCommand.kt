package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import java.time.LocalDate

data class SeriesImportCommand(
    val seriesSourceId: String,
    val seriesNameKo: String,
    val seriesReleaseDate: LocalDate
)
