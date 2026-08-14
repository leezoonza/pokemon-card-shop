package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import java.time.Instant

data class CardRegisterInfo(
    val expansionId: Long,
    val sourceId: String,
    val localId: String,
    val name: Name,
    val category: CardCategory,
    val imageUrl: String?,
    val illustrator: String?,
    val rarity: CardRarity,
    val variants: CardVariants,
    val registeredAt: Instant,
)
