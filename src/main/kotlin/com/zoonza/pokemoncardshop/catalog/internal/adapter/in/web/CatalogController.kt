package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto.SeriesWithExpansionsResponse
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogQueryUseCase
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
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
}
