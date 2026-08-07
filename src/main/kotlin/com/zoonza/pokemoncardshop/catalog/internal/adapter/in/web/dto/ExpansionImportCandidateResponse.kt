package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto

data class ExpansionImportCandidateResponse(
    val sourceId: String,
    val name: String,
    val logoUrl: String,
    val symbolUrl: String?,
    val registered: Boolean,
)
