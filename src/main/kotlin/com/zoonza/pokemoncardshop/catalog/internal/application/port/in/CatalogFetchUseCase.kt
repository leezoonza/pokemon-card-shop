package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionCandidateSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesCandidateSummary

interface CatalogFetchUseCase {
    fun fetchSeriesSummaries(): List<SeriesCandidateSummary>

    fun fetchExpansionSummaries(seriesSourceId: String): List<ExpansionCandidateSummary>
}