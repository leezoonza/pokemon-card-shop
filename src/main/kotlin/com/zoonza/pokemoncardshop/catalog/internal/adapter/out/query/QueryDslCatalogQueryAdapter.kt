package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.query

import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesExpansionRow
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogQueryPort
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
}
