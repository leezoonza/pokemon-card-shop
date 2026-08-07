package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

data class CatalogImportResult(
    val seriesId: Long,
    val expansionCount: Int,
    val cardCount: Int,
)
