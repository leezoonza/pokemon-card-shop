package com.zoonza.pokemoncardshop.catalog.internal.domain.expansion

import com.zoonza.pokemoncardshop.common.error.DomainException
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class CardCount(
    @Column(nullable = false)
    val total: Int,

    @Column(nullable = false)
    val official: Int
) {
    init {
        if (total < 0) {
            throw DomainException(ExpansionErrorCode.NEGATIVE_TOTAL_CARD_COUNT)
        }

        if (official < 0) {
            throw DomainException(ExpansionErrorCode.NEGATIVE_OFFICIAL_CARD_COUNT)
        }

        if (total < official) {
            throw DomainException(ExpansionErrorCode.OFFICIAL_CARD_COUNT_EXCEEDS_TOTAL)
        }
    }
}