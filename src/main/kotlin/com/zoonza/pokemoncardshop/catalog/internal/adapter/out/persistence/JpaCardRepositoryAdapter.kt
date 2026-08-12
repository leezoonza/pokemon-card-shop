package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.Card
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class JpaCardRepositoryAdapter(
    private val repository: CardJpaRepository,
) : CardRepository {

    override fun saveAll(cards: List<Card>): List<Card> =
        try {
            repository.saveAllAndFlush(cards)
        } catch (exception: DataIntegrityViolationException) {
            throw DomainException(CatalogImportErrorCode.SOURCE_DATA_ALREADY_REGISTERED, exception)
        }
}
