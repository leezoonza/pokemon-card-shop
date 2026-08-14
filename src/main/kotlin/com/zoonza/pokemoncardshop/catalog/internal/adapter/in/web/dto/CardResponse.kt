package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CardItem


data class CardResponse(
    val cardId: Long,
    val nameEn: String,
    val nameKo: String?,
    val rarity: String,
    val imageUrl: String?,
    val printNumber: String
) {
    constructor(
        item: CardItem
    ) : this(
        cardId = item.cardId,
        nameEn = item.nameEn,
        nameKo = item.nameKo,
        rarity = item.rarity.value,
        imageUrl = item.imageUrl,
        printNumber = item.printNumber
    )
}
