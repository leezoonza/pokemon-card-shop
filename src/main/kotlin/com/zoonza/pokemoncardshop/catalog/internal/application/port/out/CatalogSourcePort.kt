package com.zoonza.pokemoncardshop.catalog.internal.application.port.out

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceCard
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceExpansion
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceSeries
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceSeriesSummary

interface CatalogSourcePort {
    fun getSeriesSummaries(): List<SourceSeriesSummary>

    fun getSeries(sourceId: String): SourceSeries

    fun getExpansion(sourceId: String): SourceExpansion

    fun getCard(sourceId: String): SourceCard
}
