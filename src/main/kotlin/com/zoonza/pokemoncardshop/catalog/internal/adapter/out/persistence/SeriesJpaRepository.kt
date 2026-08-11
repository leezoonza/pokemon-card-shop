package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.catalog.internal.domain.series.Series
import org.springframework.data.jpa.repository.JpaRepository

interface SeriesJpaRepository : JpaRepository<Series, Long> {
    fun findBySourceId(sourceId: String): Series?
}
