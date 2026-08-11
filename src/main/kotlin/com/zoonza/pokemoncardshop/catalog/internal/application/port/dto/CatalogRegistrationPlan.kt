package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardCategory
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRarity
import java.time.LocalDate

data class CatalogRegistrationPlan(
    val series: SeriesRegistrationData,
    val expansions: List<ExpansionRegistrationData>,
)

data class SeriesRegistrationData(
    val sourceId: String,
    val name: String,
    val releaseDate: LocalDate,
)

data class ExpansionRegistrationData(
    val sourceId: String,
    val name: String,
    val totalCardCount: Int,
    val officialCardCount: Int,
    val logoUrl: String,
    val symbolUrl: String?,
    val releaseDate: LocalDate,
    val cards: List<CardRegistrationData>,
)

data class CardRegistrationData(
    val sourceId: String,
    val category: CardCategory,
    val number: String,
    val name: String,
    val imageUrl: String,
    val illustrator: String,
    val rarity: CardRarity,
    val variants: SourceCardVariants,
)
