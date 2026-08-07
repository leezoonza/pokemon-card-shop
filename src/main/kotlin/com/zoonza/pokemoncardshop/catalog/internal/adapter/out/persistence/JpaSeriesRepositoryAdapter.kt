package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.Series
import com.zoonza.pokemoncardshop.catalog.internal.domain.SeriesRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class JpaSeriesRepositoryAdapter(
    private val repository: SeriesJpaRepository,
) : SeriesRepository {

    override fun findBySourceId(sourceId: String): Series? =
        repository.findBySourceId(sourceId)

    override fun save(series: Series): Series =
        try {
            repository.saveAndFlush(series)
        } catch (exception: DataIntegrityViolationException) {
            throw DomainException(CatalogImportErrorCode.DUPLICATE_SOURCE_DATA, exception)
        }
}
