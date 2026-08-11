package com.zoonza.pokemoncardshop.catalog.internal.domain.series

interface SeriesRepository {
    fun findBySourceId(sourceId: String): Series?

    fun save(series: Series): Series
}
