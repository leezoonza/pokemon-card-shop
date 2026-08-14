package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionImportCommand
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.FetchedExpansion
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesImportCommand
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogImportUseCase
import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.stereotype.Service

@Service
class CatalogImportFacade(
    private val seriesRepository: SeriesRepository,
    private val expansionRepository: ExpansionRepository,
    private val catalogFetchService: CatalogFetchService,
    private val catalogCommandService: CatalogCommandService,
) : CatalogImportUseCase {

    override fun importSeries(command: SeriesImportCommand) {
        if (seriesRepository.existsBySourceId(command.seriesSourceId)) return

        val sourceSeries = catalogFetchService.fetchSeries(command.seriesSourceId)

        catalogCommandService.registerSeries(command, sourceSeries)
    }

    override fun importExpansionAndCard(command: ExpansionImportCommand) {
        val seriesId = seriesRepository.findBySourceId(command.seriesSourceId)?.id
            ?: throw DomainException(CatalogImportErrorCode.SERIES_NOT_REGISTERED)

        val selectedExpansions = command.expansions

        validateSelectedExpansion(selectedExpansions.map { it.expansionSourceId })
        validateUnregisteredExpansions(selectedExpansions.map { it.expansionSourceId })

        val fetchedExpansions = selectedExpansions.map { selectedExpansion ->
            val sourceExpansion = catalogFetchService.fetchExpansion(selectedExpansion.expansionSourceId)
            val cards = sourceExpansion.cardSourceIds.map { cardSourceId ->
                catalogFetchService.fetchCard(cardSourceId)
            }

            FetchedExpansion(
                source = sourceExpansion,
                cards = cards,
            )
        }

        catalogCommandService.registerExpansionsAndCards(seriesId, fetchedExpansions)
    }

    private fun validateSelectedExpansion(expansionSourceIds: List<String>) {
        if (expansionSourceIds.isEmpty()) {
            throw DomainException(CatalogImportErrorCode.EMPTY_EXPANSION_SELECTION)
        }

        if (expansionSourceIds.size != expansionSourceIds.distinct().size) {
            throw DomainException(CatalogImportErrorCode.DUPLICATE_EXPANSION_SELECTION)
        }
    }

    private fun validateUnregisteredExpansions(expansionSourceIds: List<String>) {
        if (expansionSourceIds.any(expansionRepository::existsBySourceId)) {
            throw DomainException(CatalogImportErrorCode.EXPANSION_ALREADY_REGISTERED)
        }
    }
}
