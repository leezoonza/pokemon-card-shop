package com.zoonza.pokemoncardshop.catalog.internal.domain.series

interface SeriesRepository {
    fun existsBySourceId(sourceId: String): Boolean

    fun findBySourceId(sourceId: String): Series?

    fun save(series: Series): Series
}
