package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionCandidateSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesCandidateSummary

interface CatalogCandidateFinder {
    fun findSeries(): List<SeriesCandidateSummary>

    fun findExpansions(seriesSourceId: String): List<ExpansionCandidateSummary>
}