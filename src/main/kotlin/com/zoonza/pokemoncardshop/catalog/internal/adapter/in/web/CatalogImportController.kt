package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.ExpansionCandidateResponse
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.SeriesCandidateResponse
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogCandidateFinder
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/catalog/imports")
class CatalogImportController(
    private val catalogCandidateFinder: CatalogCandidateFinder,
//    private val importCatalogUseCase: ImportCatalogUseCase,
) {

    @GetMapping("/series")
    fun getSeriesCandidates(): ApiResponse<List<SeriesCandidateResponse>> {
        val response = catalogCandidateFinder.findSeries().map {
            SeriesCandidateResponse.from(it)
        }

        return ApiResponse.success(response)
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

//    @PostMapping
//    fun importCatalog(
//        @Valid @RequestBody request: CatalogImportRequest,
//    ): ResponseEntity<ApiResponse<CatalogImportResponse>> {
//        val result = importCatalogUseCase.importCatalog(request.toCommand())
//        val response = CatalogImportResponse(
//            seriesId = result.seriesId,
//            expansionCount = result.expansionCount,
//            cardCount = result.cardCount,
//        )
//
//        return ResponseEntity
//            .status(HttpStatus.CREATED)
//            .body(ApiResponse.success(response))
//    }
}
