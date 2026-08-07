package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.CatalogImportRequest
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.CatalogImportResponse
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.ExpansionImportCandidateResponse
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.SeriesImportCandidateResponse
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.GetExpansionImportCandidatesUseCase
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.GetSeriesImportCandidatesUseCase
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.ImportCatalogUseCase
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/catalog/imports")
class CatalogImportController(
    private val getSeriesImportCandidatesUseCase: GetSeriesImportCandidatesUseCase,
    private val getExpansionImportCandidatesUseCase: GetExpansionImportCandidatesUseCase,
    private val importCatalogUseCase: ImportCatalogUseCase,
) {

    @GetMapping("/series")
    fun getSeriesCandidates(): ApiResponse<List<SeriesImportCandidateResponse>> {
        val response = getSeriesImportCandidatesUseCase.getSeriesCandidates().map { candidate ->
            SeriesImportCandidateResponse(
                sourceId = candidate.sourceId,
                name = candidate.name,
                logoUrl = candidate.logoUrl,
                registered = candidate.registered,
            )
        }
        return ApiResponse.success(response)
    }

    @GetMapping("/series/{seriesSourceId}/expansions")
    fun getExpansionCandidates(
        @PathVariable seriesSourceId: String,
    ): ApiResponse<List<ExpansionImportCandidateResponse>> {
        val response = getExpansionImportCandidatesUseCase
            .getExpansionCandidates(seriesSourceId)
            .map { candidate ->
                ExpansionImportCandidateResponse(
                    sourceId = candidate.sourceId,
                    name = candidate.name,
                    logoUrl = candidate.logoUrl,
                    symbolUrl = candidate.symbolUrl,
                    registered = candidate.registered,
                )
            }
        return ApiResponse.success(response)
    }

    @PostMapping
    fun importCatalog(
        @Valid @RequestBody request: CatalogImportRequest,
    ): ResponseEntity<ApiResponse<CatalogImportResponse>> {
        val result = importCatalogUseCase.importCatalog(request.toCommand())
        val response = CatalogImportResponse(
            seriesId = result.seriesId,
            expansionCount = result.expansionCount,
            cardCount = result.cardCount,
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }
}
