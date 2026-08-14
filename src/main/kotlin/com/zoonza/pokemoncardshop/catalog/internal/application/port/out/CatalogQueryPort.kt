package com.zoonza.pokemoncardshop.catalog.internal.application.port.out

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CardRow
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesExpansionRow

interface CatalogQueryPort {
    fun findAllExpansionRows(): List<SeriesExpansionRow>

    fun findCardRowsByCondition(expansionId: Long): List<CardRow>
}
