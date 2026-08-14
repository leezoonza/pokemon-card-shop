package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.query

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CardRow
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesExpansionRow
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogQueryPort
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.QCard
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.QExpansion
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.QSeries
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

@Component
class QueryDslCatalogQueryAdapter(
    private val clock: Clock,
    private val jpaQueryFactory: JPAQueryFactory
) : CatalogQueryPort {
    private val series = QSeries.series
    private val expansion = QExpansion.expansion
    private val card = QCard.card

    override fun findAllExpansionRows(): List<SeriesExpansionRow> {
        val now = Instant.now(clock)
        val cutoff = now.minus(7, ChronoUnit.DAYS)

        return jpaQueryFactory
            .select(
                Projections.constructor(
                    SeriesExpansionRow::class.java,
                    series.id,
                    series.name,
                    expansion.id,
                    expansion.name,
                    expansion.image.logoUrl,
                    expansion.releaseDate,
                    expansion.registeredAt.goe(cutoff)
                )
            )
            .from(expansion)
            .join(series).on(expansion.seriesId.eq(series.id))
            .orderBy(
                series.releaseDate.desc(),
                series.id.desc(),
                expansion.releaseDate.desc(),
                expansion.id.desc()
            )
            .fetch()
    }

    override fun findCardRowsByCondition(expansionId: Long): List<CardRow> {
        return jpaQueryFactory
            .select(
                Projections.constructor(
                    CardRow::class.java,
                    card.id,
                    card.name.en,
                    card.name.ko,
                    card.rarity,
                    card.imageUrl,
                    card.localId
                        .concat("/")
                        .concat(expansion.count.official.stringValue()),
                )
            )
            .from(card)
            .join(expansion).on(card.expansionId.eq(expansion.id))
            .where(card.expansionId.eq(expansionId))
            .orderBy(card.localId.asc())
            .fetch()
    }
}
