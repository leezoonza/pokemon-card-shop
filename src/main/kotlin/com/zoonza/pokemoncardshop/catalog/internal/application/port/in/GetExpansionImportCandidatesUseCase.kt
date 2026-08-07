package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionImportCandidateResult

interface GetExpansionImportCandidatesUseCase {
    fun getExpansionCandidates(seriesSourceId: String): List<ExpansionImportCandidateResult>
}
