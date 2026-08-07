package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

data class ExpansionImportCandidateResult(
    val sourceId: String,
    val name: String,
    val logoUrl: String,
    val symbolUrl: String?,
    val registered: Boolean,
)
