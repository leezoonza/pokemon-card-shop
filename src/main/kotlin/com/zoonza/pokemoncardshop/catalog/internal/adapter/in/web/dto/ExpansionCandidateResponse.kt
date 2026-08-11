package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionCandidateSummary

data class ExpansionCandidateResponse(
    val sourceId: String,
    val name: String,
    val logoUrl: String,
    val symbolUrl: String?,
    val registered: Boolean,
) {
    companion object {
        fun from(summary: ExpansionCandidateSummary): ExpansionCandidateResponse =
            ExpansionCandidateResponse(
                sourceId = summary.sourceId,
                name = summary.name,
                logoUrl = summary.logoUrl,
                symbolUrl = summary.symbolUrl,
                registered = summary.registered,
            )
    }
}
