package com.zoonza.pokemoncardshop.catalog.internal.application.port.out

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceCard
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceExpansion
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceSeries
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceSeriesSummary

interface CatalogSourcePort {
    fun fetchSeriesSummaries(): List<SourceSeriesSummary>

    fun fetchSeries(sourceId: String): SourceSeries

    fun fetchExpansion(sourceId: String): SourceExpansion

    fun fetchCard(sourceId: String): SourceCard
}
