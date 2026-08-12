package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.Series
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class JpaSeriesRepositoryAdapter(
    private val repository: SeriesJpaRepository,
) : SeriesRepository {

    override fun existsBySourceId(sourceId: String): Boolean =
        repository.existsBySourceId(sourceId)


    override fun findBySourceId(sourceId: String): Series? =
        repository.findBySourceId(sourceId)

    override fun save(series: Series): Series =
        try {
            repository.saveAndFlush(series)
        } catch (exception: DataIntegrityViolationException) {
            throw DomainException(CatalogImportErrorCode.SOURCE_DATA_ALREADY_REGISTERED, exception)
        }
}
