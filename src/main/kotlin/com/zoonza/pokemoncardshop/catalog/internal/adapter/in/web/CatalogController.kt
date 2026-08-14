package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.CardResponse
import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.SeriesWithExpansionsResponse
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogQueryUseCase
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/catalog")
class CatalogController(
    private val catalogQueryUseCase: CatalogQueryUseCase
) {
    @GetMapping("/expansions")
    fun getSeriesWithExpansions(): ApiResponse<List<SeriesWithExpansionsResponse>> {
        val result = catalogQueryUseCase.getSeriesWithExpansions()

        val response = result.map { SeriesWithExpansionsResponse(it) }

        return ApiResponse.success(response)
    }

    @GetMapping("/expansion/{expansionId}")
    fun getCards(
        @PathVariable expansionId: Long
    ): ApiResponse<List<CardResponse>> {
        val items = catalogQueryUseCase.getCards(expansionId)

        val response = items.map { CardResponse(it) }

        return ApiResponse.success(response)
    }
}
