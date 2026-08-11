package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.Expansion
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class JpaExpansionRepositoryAdapter(
    private val repository: ExpansionJpaRepository,
) : ExpansionRepository {

    override fun existsBySourceId(sourceId: String): Boolean =
        repository.existsBySourceId(sourceId)

    override fun save(expansion: Expansion): Expansion =
        try {
            repository.saveAndFlush(expansion)
        } catch (exception: DataIntegrityViolationException) {
            throw DomainException(CatalogImportErrorCode.DUPLICATE_SOURCE_DATA, exception)
        }
}
