package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.*
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.RegisterCatalogData
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourceFetcher
import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardCategory
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRarity
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.Series
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CatalogImportServiceTests {

    private val catalogSourceFetcher = mockk<CatalogSourceFetcher>()
    private val seriesRepository = mockk<SeriesRepository>()
    private val expansionRepository = mockk<ExpansionRepository>()
    private val registerCatalogData = mockk<RegisterCatalogData>()
    private val service = CatalogImportService(
        catalogSourceFetcher = catalogSourceFetcher,
        seriesRepository = seriesRepository,
        expansionRepository = expansionRepository,
        registerCatalogData = registerCatalogData,
    )

    @Test
    fun `선택한 시리즈와 확장팩의 모든 카드를 등록한다`() {
        val planSlot = slot<CatalogRegistrationPlan>()
        val command = command()
        every { catalogSourceFetcher.fetchSeries("sv") } returns sourceSeries()
        every { seriesRepository.findBySourceId("sv") } returns null
        every { expansionRepository.existsBySourceId("sv01") } returns false
        every { catalogSourceFetcher.getExpansion("sv01") } returns sourceExpansion()
        every { catalogSourceFetcher.getCard("sv01-1") } returns sourceCard()
        every { registerCatalogData.register(capture(planSlot)) } returns CatalogImportResult(
            seriesId = 1L,
            expansionCount = 1,
            cardCount = 1,
        )

        val result = service.importCatalog(command)

        result shouldBe CatalogImportResult(seriesId = 1L, expansionCount = 1, cardCount = 1)
        with(planSlot.captured) {
            series.sourceId shouldBe "sv"
            series.releaseDate shouldBe LocalDate.of(2023, 3, 31)
            expansions.single().sourceId shouldBe "sv01"
            expansions.single().symbolUrl shouldBe null
            expansions.single().cards.single().category shouldBe CardCategory.POKEMON
            expansions.single().cards.single().rarity shouldBe CardRarity.RARE
            expansions.single().cards.single().illustrator shouldBe "Unknown"
        }
        verify(exactly = 1) {
            catalogSourceFetcher.fetchSeries("sv")
            catalogSourceFetcher.getExpansion("sv01")
            catalogSourceFetcher.getCard("sv01-1")
            registerCatalogData.register(any())
        }
    }

    @Test
    fun `로고가 없는 시리즈는 등록하지 않는다`() {
        every { catalogSourceFetcher.fetchSeries("sv") } returns sourceSeries(logoUrl = null)

        val exception = shouldThrow<DomainException> {
            service.importCatalog(command())
        }

        exception.errorCode shouldBe CatalogImportErrorCode.SERIES_LOGO_REQUIRED
        verify(exactly = 0) { registerCatalogData.register(any()) }
    }

    @Test
    fun `다른 시리즈의 확장팩은 등록하지 않는다`() {
        every { catalogSourceFetcher.fetchSeries("sv") } returns sourceSeries(
            expansions = listOf(
                SourceExpansionSummary("sv02", "Paldea Evolved", "logo", null),
            ),
        )
        every { seriesRepository.findBySourceId("sv") } returns null

        val exception = shouldThrow<DomainException> {
            service.importCatalog(command())
        }

        exception.errorCode shouldBe CatalogImportErrorCode.EXPANSION_NOT_IN_SERIES
        verify(exactly = 0) {
            catalogSourceFetcher.getExpansion(any())
            registerCatalogData.register(any())
        }
    }

    @Test
    fun `로고가 없는 확장팩은 등록하지 않는다`() {
        every { catalogSourceFetcher.fetchSeries("sv") } returns sourceSeries()
        every { seriesRepository.findBySourceId("sv") } returns null
        every { expansionRepository.existsBySourceId("sv01") } returns false
        every { catalogSourceFetcher.getExpansion("sv01") } returns sourceExpansion(logoUrl = null)

        val exception = shouldThrow<DomainException> {
            service.importCatalog(command())
        }

        exception.errorCode shouldBe CatalogImportErrorCode.EXPANSION_LOGO_REQUIRED
        verify(exactly = 0) {
            catalogSourceFetcher.getCard(any())
            registerCatalogData.register(any())
        }
    }

    @Test
    fun `기존 시리즈의 출시일이 다르면 확장팩을 추가하지 않는다`() {
        every { catalogSourceFetcher.fetchSeries("sv") } returns sourceSeries()
        every { seriesRepository.findBySourceId("sv") } returns Series.register(
            sourceId = "sv",
            name = "Scarlet & Violet",
            releaseDate = LocalDate.of(2023, 1, 1),
            registeredAt = java.time.Instant.EPOCH,
        )

        val exception = shouldThrow<DomainException> {
            service.importCatalog(command())
        }

        exception.errorCode shouldBe CatalogImportErrorCode.SERIES_RELEASE_DATE_MISMATCH
        verify(exactly = 0) {
            expansionRepository.existsBySourceId(any())
            registerCatalogData.register(any())
        }
    }

    @Test
    fun `지원하지 않는 카드 희귀도가 있으면 등록하지 않는다`() {
        every { catalogSourceFetcher.fetchSeries("sv") } returns sourceSeries()
        every { seriesRepository.findBySourceId("sv") } returns null
        every { expansionRepository.existsBySourceId("sv01") } returns false
        every { catalogSourceFetcher.getExpansion("sv01") } returns sourceExpansion()
        every { catalogSourceFetcher.getCard("sv01-1") } returns sourceCard(rarity = "Unknown Rare")

        val exception = shouldThrow<DomainException> {
            service.importCatalog(command())
        }

        exception.errorCode shouldBe CatalogImportErrorCode.UNSUPPORTED_CARD_RARITY
        verify(exactly = 0) { registerCatalogData.register(any()) }
    }

    private fun command() = CatalogImportCommand(
        seriesSourceId = "sv",
        seriesReleaseDate = LocalDate.of(2023, 3, 31),
        expansionSourceIds = listOf("sv01"),
    )

    private fun sourceSeries(
        logoUrl: String? = "https://image/sv.png",
        expansions: List<SourceExpansionSummary> = listOf(
            SourceExpansionSummary("sv01", "Scarlet & Violet", "logo", null),
        ),
    ) = SourceSeries(
        sourceId = "sv",
        name = "Scarlet & Violet",
        logoUrl = logoUrl,
        expansions = expansions,
    )

    private fun sourceExpansion(
        logoUrl: String? = "https://image/sv01.png",
    ) = SourceExpansion(
        sourceId = "sv01",
        seriesSourceId = "sv",
        name = "Scarlet & Violet",
        logoUrl = logoUrl,
        symbolUrl = null,
        releaseDate = LocalDate.of(2023, 3, 31),
        totalCardCount = 258,
        officialCardCount = 198,
        cardSourceIds = listOf("sv01-1"),
    )

    private fun sourceCard(
        rarity: String = "Rare",
    ) = SourceCard(
        sourceId = "sv01-1",
        expansionSourceId = "sv01",
        category = "Pokemon",
        number = "1",
        name = "Pikachu",
        imageUrl = "https://image/sv01-1/high.webp",
        illustrator = null,
        rarity = rarity,
        variants = SourceCardVariants(
            firstEdition = false,
            holo = true,
            normal = false,
            reverse = true,
            wPromo = false,
        ),
    )
}
