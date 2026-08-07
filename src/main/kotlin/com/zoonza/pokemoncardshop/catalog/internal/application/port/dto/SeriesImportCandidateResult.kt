package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

data class SeriesImportCandidateResult(
    val sourceId: String,
    val name: String,
    val logoUrl: String,
    val registered: Boolean,
)
