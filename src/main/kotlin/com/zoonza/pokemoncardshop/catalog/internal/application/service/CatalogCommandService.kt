package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.FetchedCard
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.FetchedExpansion
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesImportCommand
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceSeries
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.Card
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.Expansion
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.Series
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
class CatalogCommandService(
    private val clock: Clock,
    private val seriesRepository: SeriesRepository,
    private val expansionRepository: ExpansionRepository,
    private val cardRepository: CardRepository,
) {
    fun registerSeries(command: SeriesImportCommand, sourceSeries: SourceSeries) {
        val series = Series.register(
            sourceId = command.seriesSourceId,
            name = Name(sourceSeries.name, command.seriesNameKo),
            releaseDate = command.seriesReleaseDate,
            registeredAt = Instant.now(clock),
        )

        seriesRepository.save(series)
    }

    @Transactional
    fun registerExpansionsAndCards(seriesId: Long, fetchedExpansions: List<FetchedExpansion>) {
        fetchedExpansions.forEach { fetchedExpansion ->
            val expansionId = registerExpansion(seriesId, fetchedExpansion)

            registerCards(expansionId, fetchedExpansion.cards)
        }
    }

    private fun registerExpansion(seriesId: Long, fetchedExpansion: FetchedExpansion): Long {
        val info = fetchedExpansion.toExpansionRegisterInfo(
            seriesId = seriesId,
            registeredAt = Instant.now(clock),
        )
        val expansion = Expansion.register(info)

        return expansionRepository.save(expansion).id
    }

    private fun registerCards(expansionId: Long, fetchedCards: List<FetchedCard>) {
        val cards = fetchedCards.map { fetchedCard ->
            createCard(expansionId, fetchedCard)
        }

        cardRepository.saveAll(cards)
    }

    private fun createCard(expansionId: Long, fetchedCard: FetchedCard): Card {
        val info = fetchedCard.toCardRegisterInfo(
            expansionId = expansionId,
            registeredAt = Instant.now(clock),
        )

        return Card.register(info)
    }
}
