package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionImportCandidateResult
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesImportCandidateResult
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.GetExpansionImportCandidatesUseCase
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.GetSeriesImportCandidatesUseCase
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourcePort
import com.zoonza.pokemoncardshop.catalog.internal.domain.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.SeriesRepository
import org.springframework.stereotype.Service

@Service
class CatalogImportQueryService(
    private val catalogSourcePort: CatalogSourcePort,
    private val seriesRepository: SeriesRepository,
    private val expansionRepository: ExpansionRepository,
) : GetSeriesImportCandidatesUseCase, GetExpansionImportCandidatesUseCase {

    override fun getSeriesCandidates(): List<SeriesImportCandidateResult> =
        catalogSourcePort.getSeriesSummaries()
            .mapNotNull { series ->
                val logoUrl = series.logoUrl?.takeIf(String::isNotBlank)
                    ?: return@mapNotNull null

                SeriesImportCandidateResult(
                    sourceId = series.sourceId,
                    name = series.name,
                    logoUrl = logoUrl,
                    registered = seriesRepository.findBySourceId(series.sourceId) != null,
                )
            }

    override fun getExpansionCandidates(seriesSourceId: String): List<ExpansionImportCandidateResult> =
        catalogSourcePort.getSeries(seriesSourceId)
            .expansions
            .mapNotNull { expansion ->
                val logoUrl = expansion.logoUrl?.takeIf(String::isNotBlank)
                    ?: return@mapNotNull null

                ExpansionImportCandidateResult(
                    sourceId = expansion.sourceId,
                    name = expansion.name,
                    logoUrl = logoUrl,
                    symbolUrl = expansion.symbolUrl?.takeIf(String::isNotBlank),
                    registered = expansionRepository.existsBySourceId(expansion.sourceId),
                )
            }
}
