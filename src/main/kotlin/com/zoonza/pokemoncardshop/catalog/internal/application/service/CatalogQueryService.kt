package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionItem
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesWithExpansions
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogQueryUseCase
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogQueryPort
import org.springframework.stereotype.Service

@Service
class CatalogQueryService(
    private val catalogQueryPort: CatalogQueryPort
) : CatalogQueryUseCase {

    override fun getSeriesWithExpansions(): List<SeriesWithExpansions> =
        catalogQueryPort.findAllExpansionRows()
            .groupBy { it.seriesId }
            .values
            .map { seriesRows ->
                val series = seriesRows.first()

                SeriesWithExpansions(
                    seriesId = series.seriesId,
                    seriesName = series.seriesName,
                    expansions = seriesRows.map {
                        ExpansionItem(
                            expansionId = it.expansionId,
                            name = it.expansionName,
                            logoUrl = it.expansionLogoUrl,
                            releaseDate = it.expansionReleaseDate,
                            isNew = it.isNew
                        )
                    }
                )
            }
}
