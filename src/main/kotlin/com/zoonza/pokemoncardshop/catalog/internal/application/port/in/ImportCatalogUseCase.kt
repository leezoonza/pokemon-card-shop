package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportCommand
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportResult

interface ImportCatalogUseCase {
    fun importCatalog(command: CatalogImportCommand): CatalogImportResult
}
