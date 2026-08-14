package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CardNameTranslationPort
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourcePort
import com.zoonza.pokemoncardshop.catalog.internal.application.service.CatalogFetchService
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import com.zoonza.pokemoncardshop.catalog.test.fake.sourceSeriesSummaryFixture
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
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
    fun `등록된 시리즈는 등록 상태를 포함해 조회한다`() {
        every { catalogSourcePort.fetchSeriesSummaries() } returns listOf(
            sourceSeriesSummaryFixture(),
        )
        every { seriesRepository.existsBySourceId("sv") } returns true

        val result = catalogFetchUseCase.fetchSeriesSummaries()

        result.single().registered shouldBe true
    }
}
