package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportResult
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogRegistrationPlan

interface RegisterCatalogData {
    fun register(plan: CatalogRegistrationPlan): CatalogImportResult
}
