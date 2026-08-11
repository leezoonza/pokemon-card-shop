package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceExpansionSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceSeries
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SourceSeriesSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourceFetcher
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CatalogImportQueryServiceTests {

    private val catalogSourceFetcher = mockk<CatalogSourceFetcher>()
    private val seriesRepository = mockk<SeriesRepository>()
    private val expansionRepository = mockk<ExpansionRepository>()
    private val service = CatalogImportQueryService(
        catalogSourceFetcher = catalogSourceFetcher,
        seriesRepository = seriesRepository,
        expansionRepository = expansionRepository,
    )

    @Test
    fun `로고가 있는 시리즈만 등록 후보로 조회한다`() {
        every { catalogSourceFetcher.fetchSeriesSummaries() } returns listOf(
            SourceSeriesSummary("sv", "Scarlet & Violet", "https://image/sv.png"),
            SourceSeriesSummary("none", "No Logo", null),
            SourceSeriesSummary("blank", "Blank Logo", ""),
        )
        every { seriesRepository.findBySourceId("sv") } returns null

        val result = service.findSeries()

        result.map { it.sourceId } shouldContainExactly listOf("sv")
        result.single().logoUrl shouldBe "https://image/sv.png"
        result.single().registered shouldBe false
        verify(exactly = 1) { seriesRepository.findBySourceId("sv") }
        verify(exactly = 0) {
            seriesRepository.findBySourceId("none")
            seriesRepository.findBySourceId("blank")
        }
    }

    @Test
    fun `선택한 시리즈에서 로고가 있는 확장팩만 조회한다`() {
        every { catalogSourceFetcher.fetchSeries("sv") } returns SourceSeries(
            sourceId = "sv",
            name = "Scarlet & Violet",
            logoUrl = "https://image/sv.png",
            expansions = listOf(
                SourceExpansionSummary(
                    sourceId = "sv01",
                    name = "Scarlet & Violet",
                    logoUrl = "https://image/sv01.png",
                    symbolUrl = null,
                ),
                SourceExpansionSummary(
                    sourceId = "sv02",
                    name = "Paldea Evolved",
                    logoUrl = null,
                    symbolUrl = "https://image/sv02-symbol.png",
                ),
            ),
        )
        every { expansionRepository.existsBySourceId("sv01") } returns true

        val result = service.findExpansions("sv")

        result.map { it.sourceId } shouldContainExactly listOf("sv01")
        result.single().registered shouldBe true
        result.single().symbolUrl shouldBe null
        verify(exactly = 0) { expansionRepository.existsBySourceId("sv02") }
    }
}
