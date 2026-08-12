package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.ExpansionCandidateResponse
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.ExpansionImportRequest
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.SeriesCandidateResponse
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.SeriesImportRequest
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogCandidateFinder
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogImporter
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/catalog/imports")
class CatalogImportController(
    private val catalogCandidateFinder: CatalogCandidateFinder,
    private val catalogImporter: CatalogImporter,
) {
    @GetMapping("/series")
    fun getSeriesCandidates(): ApiResponse<List<SeriesCandidateResponse>> {
        val response = catalogCandidateFinder.findSeries().map {
            SeriesCandidateResponse.from(it)
        }

        return ApiResponse.success(response)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/series")
    fun importSeries(
        @Valid @RequestBody request: SeriesImportRequest
    ): ApiResponse<Unit> {
        catalogImporter.importSeries(request.toCommand())

        return ApiResponse.success()
    }

    @GetMapping("/series/{seriesSourceId}/expansions")
    fun getExpansionCandidates(
        @PathVariable seriesSourceId: String,
    ): ApiResponse<List<ExpansionCandidateResponse>> {
        val response = catalogCandidateFinder.findExpansions(seriesSourceId).map {
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
        catalogImporter.importExpansionAndCard(request.toCommand(seriesSourceId))

        return ApiResponse.success()
    }
}
