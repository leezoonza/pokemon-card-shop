package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionItem
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesExpansionRow
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesWithExpansions
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogQueryPort
import com.zoonza.pokemoncardshop.catalog.internal.application.service.CatalogQueryService
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CatalogQueryUseCaseTests {

    private val catalogQueryPort = mockk<CatalogQueryPort>()
    private val catalogQueryUseCase: CatalogQueryUseCase = CatalogQueryService(catalogQueryPort)

    @Test
    fun `조회 행을 시리즈별 확장팩 목록으로 묶는다`() {
        every { catalogQueryPort.findAllExpansionRows() } returns listOf(
            seriesExpansionRow(
                seriesId = 2L,
                seriesName = "Scarlet & Violet",
                expansionId = 21L,
                expansionName = "Paldea Evolved",
                releaseDate = LocalDate.of(2023, 6, 9),
                isNew = true,
            ),
            seriesExpansionRow(
                seriesId = 2L,
                seriesName = "Scarlet & Violet",
                expansionId = 20L,
                expansionName = "Scarlet & Violet",
                releaseDate = LocalDate.of(2023, 3, 31),
                isNew = false,
            ),
            seriesExpansionRow(
                seriesId = 1L,
                seriesName = "Sword & Shield",
                expansionId = 10L,
                expansionName = "Sword & Shield",
                releaseDate = LocalDate.of(2020, 2, 7),
                isNew = false,
            ),
        )

        val result = catalogQueryUseCase.getSeriesWithExpansions()

        result shouldContainExactly listOf(
            SeriesWithExpansions(
                seriesId = 2L,
                seriesName = "Scarlet & Violet",
                expansions = listOf(
                    ExpansionItem(
                        21L,
                        "Paldea Evolved",
                        "https://image/21.png",
                        LocalDate.of(2023, 6, 9),
                        true
                    ),
                    ExpansionItem(
                        20L,
                        "Scarlet & Violet",
                        "https://image/20.png",
                        LocalDate.of(2023, 3, 31),
                        false
                    ),
                ),
            ),
            SeriesWithExpansions(
                seriesId = 1L,
                seriesName = "Sword & Shield",
                expansions = listOf(
                    ExpansionItem(
                        10L,
                        "Sword & Shield",
                        "https://image/10.png",
                        LocalDate.of(2020, 2, 7),
                        false
                    ),
                ),
            ),
        )
        verify(exactly = 1) { catalogQueryPort.findAllExpansionRows() }
    }

    private fun seriesExpansionRow(
        seriesId: Long,
        seriesName: String,
        expansionId: Long,
        expansionName: String,
        releaseDate: LocalDate,
        isNew: Boolean,
    ) = SeriesExpansionRow(
        seriesId = seriesId,
        seriesName = seriesName,
        expansionId = expansionId,
        expansionName = expansionName,
        expansionLogoUrl = "https://image/$expansionId.png",
        expansionReleaseDate = releaseDate,
        isNew = isNew,
    )
}
