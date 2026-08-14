package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesCandidateSummary

data class SeriesCandidateResponse(
    val sourceId: String,
    val name: String,
    val logoUrl: String?,
    val registered: Boolean,
) {
    companion object {
        fun from(summary: SeriesCandidateSummary): SeriesCandidateResponse =
            SeriesCandidateResponse(
                sourceId = summary.sourceId,
                name = summary.name,
                logoUrl = summary.logoUrl,
                registered = summary.registered,
            )
    }
}
