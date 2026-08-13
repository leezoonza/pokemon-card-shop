package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.ExpansionCandidateResponse
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.ExpansionImportRequest
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.SeriesCandidateResponse
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.SeriesImportRequest
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogFetchUseCase
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogImportUseCase
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/catalog/imports")
class CatalogImportController(
    private val catalogFetchUseCase: CatalogFetchUseCase,
    private val catalogImportUseCase: CatalogImportUseCase,
) {
    @GetMapping("/series")
    fun fetchSeriesCandidates(): ApiResponse<List<SeriesCandidateResponse>> {
        val response = catalogFetchUseCase.fetchSeriesSummaries().map {
            SeriesCandidateResponse.from(it)
        }

        return ApiResponse.success(response)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/series")
    fun importSeries(
        @Valid @RequestBody request: SeriesImportRequest
    ): ApiResponse<Unit> {
        catalogImportUseCase.importSeries(request.toCommand())

        return ApiResponse.success()
    }

    @GetMapping("/series/{seriesSourceId}/expansions")
    fun fetchExpansionCandidates(
        @PathVariable seriesSourceId: String,
    ): ApiResponse<List<ExpansionCandidateResponse>> {
        val response = catalogFetchUseCase.fetchExpansionSummaries(seriesSourceId).map {
            ExpansionCandidateResponse.from(it)
        }

        return ApiResponse.success(response)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/series/{seriesSourceId}/expansions")
    fun importExpansionAndCard(
        @PathVariable seriesSourceId: String,
        @Valid @RequestBody request: ExpansionImportRequest,
    ): ApiResponse<Unit> {
        catalogImportUseCase.importExpansionAndCard(request.toCommand(seriesSourceId))

        return ApiResponse.success()
    }
}
