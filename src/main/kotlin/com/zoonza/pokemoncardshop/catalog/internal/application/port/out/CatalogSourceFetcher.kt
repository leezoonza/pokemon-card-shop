package com.zoonza.pokemoncardshop.catalog.internal.application.port.out

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceCard
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceExpansion
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceSeries
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceSeriesSummary

interface CatalogSourceFetcher {
    fun fetchSeriesSummaries(): List<SourceSeriesSummary>

    fun fetchSeries(sourceId: String): SourceSeries

    fun getExpansion(sourceId: String): SourceExpansion

    fun getCard(sourceId: String): SourceCard
}
