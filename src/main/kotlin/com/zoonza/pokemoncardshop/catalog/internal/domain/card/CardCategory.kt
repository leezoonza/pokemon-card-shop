package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.common.error.DomainException

enum class CardCategory(val value: String) {
    POKEMON("Pokemon"),
    TRAINER("Trainer"),
    ENERGY("Energy"),
    ;

    companion object {
        fun from(value: String): CardCategory =
            entries.find { it.value == value }
                ?: throw DomainException(CardErrorCode.NOT_SUPPORTED_CATEGORY)
    }
}