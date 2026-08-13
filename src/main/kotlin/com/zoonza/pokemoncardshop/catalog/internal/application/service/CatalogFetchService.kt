package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.*
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogFetchUseCase
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CardNameTranslationPort
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourcePort
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import org.springframework.stereotype.Service

@Service
class CatalogFetchService(
    private val seriesRepository: SeriesRepository,
    private val expansionRepository: ExpansionRepository,
    private val catalogSourcePort: CatalogSourcePort,
    private val cardNameTranslationPort: CardNameTranslationPort,
) : CatalogFetchUseCase {

    override fun fetchSeriesSummaries(): List<SeriesCandidateSummary> =
        catalogSourcePort.fetchSeriesSummaries()
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

    override fun fetchExpansionSummaries(seriesSourceId: String): List<ExpansionCandidateSummary> =
        catalogSourcePort.fetchSeries(seriesSourceId)
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

    fun fetchSeries(seriesSourceId: String): SourceSeries =
        catalogSourcePort.fetchSeries(seriesSourceId)

    fun fetchExpansion(expansionSourceId: String): SourceExpansion =
        catalogSourcePort.fetchExpansion(expansionSourceId)

    fun fetchCard(cardSourceId: String): FetchedCard {
        val sourceCard = catalogSourcePort.fetchCard(cardSourceId)

        return FetchedCard(
            source = sourceCard,
            nameKo = cardNameTranslationPort.translate(sourceCard.name),
        )
    }
}
