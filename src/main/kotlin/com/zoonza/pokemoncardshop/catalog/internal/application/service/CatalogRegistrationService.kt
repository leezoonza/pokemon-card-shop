//package com.zoonza.pokemoncardshop.catalog.internal.application.service
//
//import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportResult
//import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogRegistrationPlan
//import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.RegisterCatalogData
//import com.zoonza.pokemoncardshop.catalog.internal.domain.*
//import com.zoonza.pokemoncardshop.common.error.DomainException
//import org.springframework.stereotype.Service
//import org.springframework.transaction.annotation.Transactional
//import java.time.Clock
//import java.time.Instant
//
//@Service
//class CatalogRegistrationService(
//    private val seriesRepository: SeriesRepository,
//    private val expansionRepository: ExpansionRepository,
//    private val cardRepository: CardRepository,
//    private val clock: Clock,
//) : RegisterCatalogData {
//
//    @Transactional
//    override fun register(plan: CatalogRegistrationPlan): CatalogImportResult {
//        val registeredAt = Instant.now(clock)
//        val series = findOrCreateSeries(plan, registeredAt)
//        var cardCount = 0
//
//        plan.expansions.forEach { data ->
//            if (expansionRepository.existsBySourceId(data.sourceId)) {
//                throw DomainException(CatalogImportErrorCode.EXPANSION_ALREADY_REGISTERED)
//            }
//            val expansion = expansionRepository.save(
//                Expansion.register(
//                    seriesId = series.id,
//                    sourceId = data.sourceId,
//                    name = data.name,
//                    count = CardCount(
//                        total = data.totalCardCount,
//                        official = data.officialCardCount,
//                    ),
//                    image = ExpansionImage(
//                        logoUrl = data.logoUrl,
//                        symbolUrl = data.symbolUrl.orEmpty(),
//                    ),
//                    releaseDate = data.releaseDate,
//                    registeredAt = registeredAt,
//                ),
//            )
//            val cards = data.cards.map { card ->
//                Card.register(
//                    expansionId = expansion.id,
//                    sourceId = card.sourceId,
//                    category = card.category,
//                    localId = card.number,
//                    name = card.name,
//                    imageUrl = card.imageUrl,
//                    illustrator = card.illustrator,
//                    rarity = card.rarity,
//                    variants = CardVariants(
//                        firstEdition = card.variants.firstEdition,
//                        holo = card.variants.holo,
//                        normal = card.variants.normal,
//                        reverse = card.variants.reverse,
//                        wPromo = card.variants.wPromo,
//                    ),
//                    registeredAt = registeredAt,
//                )
//            }
//            cardRepository.saveAll(cards)
//            cardCount += cards.size
//        }
//
//        return CatalogImportResult(
//            seriesId = series.id,
//            expansionCount = plan.expansions.size,
//            cardCount = cardCount,
//        )
//    }
//
//    private fun findOrCreateSeries(
//        plan: CatalogRegistrationPlan,
//        registeredAt: Instant,
//    ): Series {
//        val data = plan.series
//        val existingSeries = seriesRepository.findBySourceId(data.sourceId)
//        if (existingSeries != null) {
//            if (existingSeries.releaseDate != data.releaseDate) {
//                throw DomainException(CatalogImportErrorCode.SERIES_RELEASE_DATE_MISMATCH)
//            }
//            return existingSeries
//        }
//
//        return seriesRepository.save(
//            Series.register(
//                sourceId = data.sourceId,
//                name = data.name,
//                releaseDate = data.releaseDate,
//                registeredAt = registeredAt,
//            ),
//        )
//    }
//}
