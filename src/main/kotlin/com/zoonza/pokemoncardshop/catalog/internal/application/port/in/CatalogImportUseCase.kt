package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionImportCommand
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesImportCommand

interface CatalogImportUseCase {
    fun importSeries(command: SeriesImportCommand)

    fun importExpansionAndCard(command: ExpansionImportCommand)
}
