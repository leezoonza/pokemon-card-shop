package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesImportCandidateResult

interface GetSeriesImportCandidatesUseCase {
    fun getSeriesCandidates(): List<SeriesImportCandidateResult>
}
