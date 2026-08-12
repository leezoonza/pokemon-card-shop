package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto


data class ExpansionImportCommand(
    val seriesSourceId: String,
    val expansions: List<ExpansionImportSelectionCommand>
)

data class ExpansionImportSelectionCommand(
    val expansionSourceId: String,
    val expansionNameKo: String,
)
