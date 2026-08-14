package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRarity

data class CardRow(
    val cardId: Long,
    val nameEn: String,
    val nameKo: String?,
    val rarity: CardRarity,
    val imageUrl: String?,
    val printNumber: String
)
