package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

data class ExpansionCandidateSummary(
    val sourceId: String,
    val name: String,
    val logoUrl: String,
    val symbolUrl: String?,
    val registered: Boolean,
)
