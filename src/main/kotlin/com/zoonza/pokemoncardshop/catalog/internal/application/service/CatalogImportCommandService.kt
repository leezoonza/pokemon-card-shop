package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.*
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogImporter
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CardNameTranslator
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourceFetcher
import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.Card
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.Expansion
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.Series
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class CatalogImportCommandService(
    private val clock: Clock,
    private val seriesRepository: SeriesRepository,
    private val expansionRepository: ExpansionRepository,
    private val cardRepository: CardRepository,
    private val catalogSourceFetcher: CatalogSourceFetcher,
    private val cardNameTranslator: CardNameTranslator,
) : CatalogImporter {

    override fun importSeries(command: SeriesImportCommand) {
        if (seriesRepository.existsBySourceId(command.seriesSourceId)) return

        val sourceSeries = catalogSourceFetcher.fetchSeries(command.seriesSourceId)

        val series = Series.register(
            sourceId = command.seriesSourceId,
            name = Name(sourceSeries.name, command.seriesNameKo),
            releaseDate = command.seriesReleaseDate,
            registeredAt = Instant.now(clock)
        )

        seriesRepository.save(series)
    }

    @Transactional
    override fun importExpansionAndCard(command: ExpansionImportCommand) {
        val series = seriesRepository.findBySourceId(command.seriesSourceId)
            ?: throw DomainException(CatalogImportErrorCode.SERIES_NOT_REGISTERED)

        val selectedExpansions = command.expansions

        validateSelectedExpansion(selectedExpansions.map { it.expansionSourceId })

        selectedExpansions.forEach { selectedExpansion ->
            if (expansionRepository.existsBySourceId(selectedExpansion.expansionSourceId)) {
                throw DomainException(CatalogImportErrorCode.EXPANSION_ALREADY_REGISTERED)
            }

            val sourceExpansion = catalogSourceFetcher.fetchExpansion(selectedExpansion.expansionSourceId)

            val registeredExpansionId = registerExpansion(
                series.id,
                sourceExpansion,
                selectedExpansion
            )

            val cards = sourceExpansion.cardSourceIds.map { cardSourceId ->
                val sourceCard = catalogSourceFetcher.fetchCard(cardSourceId)

                createCard(registeredExpansionId, sourceCard)
            }

            cardRepository.saveAll(cards)
        }
    }

    private fun validateSelectedExpansion(expansionSourceIds: List<String>) {
        if (expansionSourceIds.isEmpty()) {
            throw DomainException(CatalogImportErrorCode.EMPTY_EXPANSION_SELECTION)
        }

        if (expansionSourceIds.size != expansionSourceIds.distinct().size) {
            throw DomainException(CatalogImportErrorCode.DUPLICATE_EXPANSION_SELECTION)
        }
    }

    private fun registerExpansion(
        seriesId: Long,
        sourceExpansion: SourceExpansion,
        selectedExpansion: ExpansionImportSelectionCommand
    ): Long {
        val info = sourceExpansion.toExpansionRegisterInfo(
            seriesId = seriesId,
            nameKo = selectedExpansion.expansionNameKo,
            registeredAt = Instant.now(clock),
        )

        val expansion = Expansion.register(info)

        return expansionRepository.save(expansion).id
    }

    private fun createCard(expansionId: Long, sourceCard: SourceCard): Card {
        val info = sourceCard.toCardRegisterInfo(
            expansionId = expansionId,
            nameKo = cardNameTranslator.translate(sourceCard.name),
            registeredAt = Instant.now(clock)
        )

        return Card.register(info)
    }
}
