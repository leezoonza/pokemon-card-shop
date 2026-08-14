package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CardItem
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesWithExpansions

interface CatalogQueryUseCase {
    fun getSeriesWithExpansions(): List<SeriesWithExpansions>

    fun getCards(expansionId: Long): List<CardItem>
}
