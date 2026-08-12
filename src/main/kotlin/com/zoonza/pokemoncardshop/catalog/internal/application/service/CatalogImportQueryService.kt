package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionCandidateSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesCandidateSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogCandidateFinder
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourceFetcher
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import org.springframework.stereotype.Service

@Service
class CatalogImportQueryService(
    private val seriesRepository: SeriesRepository,
    private val expansionRepository: ExpansionRepository,
    private val catalogSourceFetcher: CatalogSourceFetcher,
) : CatalogCandidateFinder {

    override fun findSeries(): List<SeriesCandidateSummary> =
        catalogSourceFetcher.fetchSeriesSummaries()
            .mapNotNull { series ->
                val logoUrl = series.logoUrl
                    ?.takeIf(String::isNotBlank)
                    ?: return@mapNotNull null

                val registered = seriesRepository.existsBySourceId(series.sourceId)

                SeriesCandidateSummary(
                    sourceId = series.sourceId,
                    name = series.name,
                    logoUrl = logoUrl,
                    registered = registered,
                )
            }

    override fun findExpansions(seriesSourceId: String): List<ExpansionCandidateSummary> =
        catalogSourceFetcher.fetchSeries(seriesSourceId)
            .expansions
            .mapNotNull { expansion ->
                val logoUrl = expansion.logoUrl
                    ?.takeIf(String::isNotBlank)
                    ?: return@mapNotNull null

                val registered = expansionRepository.existsBySourceId(expansion.sourceId)

                ExpansionCandidateSummary(
                    sourceId = expansion.sourceId,
                    name = expansion.name,
                    logoUrl = logoUrl,
                    symbolUrl = expansion.symbolUrl?.takeIf(String::isNotBlank),
                    registered = registered,
                )
            }
}
