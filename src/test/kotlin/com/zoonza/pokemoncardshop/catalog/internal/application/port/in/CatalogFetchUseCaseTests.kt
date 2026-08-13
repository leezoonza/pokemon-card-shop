package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CardNameTranslationPort
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourcePort
import com.zoonza.pokemoncardshop.catalog.internal.application.service.CatalogFetchService
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import com.zoonza.pokemoncardshop.catalog.test.fake.sourceExpansionSummaryFixture
import com.zoonza.pokemoncardshop.catalog.test.fake.sourceSeriesFixture
import com.zoonza.pokemoncardshop.catalog.test.fake.sourceSeriesSummaryFixture
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class CatalogFetchUseCaseTests {

    private val catalogSourcePort = mockk<CatalogSourcePort>()
    private val cardNameTranslationPort = mockk<CardNameTranslationPort>()
    private val seriesRepository = mockk<SeriesRepository>()
    private val expansionRepository = mockk<ExpansionRepository>()
    private val catalogFetchUseCase: CatalogFetchUseCase = CatalogFetchService(
        catalogSourcePort = catalogSourcePort,
        seriesRepository = seriesRepository,
        expansionRepository = expansionRepository,
        cardNameTranslationPort = cardNameTranslationPort,
    )

    @Test
    fun `로고가 있는 시리즈만 등록 후보로 조회한다`() {
        every { catalogSourcePort.fetchSeriesSummaries() } returns listOf(
            sourceSeriesSummaryFixture(),
            sourceSeriesSummaryFixture(sourceId = "none", name = "No Logo", logoUrl = null),
            sourceSeriesSummaryFixture(sourceId = "blank", name = "Blank Logo", logoUrl = ""),
        )
        every { seriesRepository.existsBySourceId("sv") } returns false

        val result = catalogFetchUseCase.fetchSeriesSummaries()

        result.map { it.sourceId } shouldContainExactly listOf("sv")
        result.single().logoUrl shouldBe "https://image/sv.png"
        result.single().registered shouldBe false

        verify(exactly = 1) { seriesRepository.existsBySourceId("sv") }
        verify(exactly = 0) {
            seriesRepository.existsBySourceId("none")
            seriesRepository.existsBySourceId("blank")
        }
    }

    @Test
    fun `등록된 시리즈는 등록 상태를 포함해 조회한다`() {
        every { catalogSourcePort.fetchSeriesSummaries() } returns listOf(
            sourceSeriesSummaryFixture(),
        )
        every { seriesRepository.existsBySourceId("sv") } returns true

        val result = catalogFetchUseCase.fetchSeriesSummaries()

        result.single().registered shouldBe true
    }

    @Test
    fun `선택한 시리즈에서 로고가 있는 확장팩만 조회한다`() {
        every { catalogSourcePort.fetchSeries("sv") } returns sourceSeriesFixture(
            expansions = listOf(
                sourceExpansionSummaryFixture(symbolUrl = null),
                sourceExpansionSummaryFixture(
                    sourceId = "sv02",
                    name = "Paldea Evolved",
                    logoUrl = null,
                ),
                sourceExpansionSummaryFixture(
                    sourceId = "sv03",
                    name = "Obsidian Flames",
                    logoUrl = "",
                ),
            ),
        )
        every { expansionRepository.existsBySourceId("sv01") } returns true

        val result = catalogFetchUseCase.fetchExpansionSummaries("sv")

        result.map { it.sourceId } shouldContainExactly listOf("sv01")
        result.single().registered shouldBe true
        result.single().symbolUrl shouldBe null

        verify(exactly = 0) {
            expansionRepository.existsBySourceId("sv02")
            expansionRepository.existsBySourceId("sv03")
        }
    }
}
