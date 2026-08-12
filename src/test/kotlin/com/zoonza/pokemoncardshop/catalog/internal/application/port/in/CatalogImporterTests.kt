package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CardNameTranslator
import com.zoonza.pokemoncardshop.catalog.internal.application.port.out.CatalogSourceFetcher
import com.zoonza.pokemoncardshop.catalog.internal.application.service.CatalogImportCommandService
import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.*
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.CardCount
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.Expansion
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionImage
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.Series
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.SeriesRepository
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.catalog.test.fake.*
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

class CatalogImporterTests {

    private val catalogSourceFetcher = mockk<CatalogSourceFetcher>()
    private val cardNameTranslator = mockk<CardNameTranslator>()
    private val seriesRepository = mockk<SeriesRepository>()
    private val expansionRepository = mockk<ExpansionRepository>()
    private val cardRepository = mockk<CardRepository>()
    private val catalogImporter: CatalogImporter = CatalogImportCommandService(
        catalogSourceFetcher = catalogSourceFetcher,
        cardNameTranslator = cardNameTranslator,
        seriesRepository = seriesRepository,
        expansionRepository = expansionRepository,
        cardRepository = cardRepository,
        clock = Clock.fixed(TEST_REGISTERED_AT, ZoneOffset.UTC),
    )

    @Test
    fun `신규 시리즈를 등록한다`() {
        val seriesSlot = slot<Series>()
        val command = seriesImportCommandFixture()

        every { seriesRepository.existsBySourceId("sv") } returns false
        every { catalogSourceFetcher.fetchSeries("sv") } returns sourceSeriesFixture()
        every { seriesRepository.save(capture(seriesSlot)) } answers { firstArg() }

        catalogImporter.importSeries(command)

        with(seriesSlot.captured) {
            sourceId shouldBe "sv"
            name shouldBe Name(en = "Scarlet & Violet", ko = "스칼렛&바이올렛")
            releaseDate shouldBe LocalDate.of(2023, 3, 31)
            registeredAt shouldBe TEST_REGISTERED_AT
        }

        verify(exactly = 1) {
            seriesRepository.existsBySourceId("sv")
            catalogSourceFetcher.fetchSeries("sv")
            seriesRepository.save(any())
        }
    }

    @Test
    fun `이미 등록된 시리즈는 다시 등록하지 않는다`() {
        every { seriesRepository.existsBySourceId("sv") } returns true

        catalogImporter.importSeries(seriesImportCommandFixture())

        verify(exactly = 1) { seriesRepository.existsBySourceId("sv") }
        verify(exactly = 0) {
            catalogSourceFetcher.fetchSeries(any())
            seriesRepository.save(any())
        }
    }

    @Test
    fun `선택한 확장팩과 모든 카드를 등록한다`() {
        val expansionSlot = slot<Expansion>()
        val cardsSlot = slot<List<Card>>()
        val series = seriesFixture()

        every { seriesRepository.findBySourceId("sv") } returns series
        every { expansionRepository.existsBySourceId("sv01") } returns false
        every { catalogSourceFetcher.fetchExpansion("sv01") } returns sourceExpansionFixture(
            cardSourceIds = listOf("sv01-1", "sv01-2"),
        )
        every { expansionRepository.save(capture(expansionSlot)) } answers { firstArg() }
        every { catalogSourceFetcher.fetchCard("sv01-1") } returns sourceCardFixture()
        every { catalogSourceFetcher.fetchCard("sv01-2") } returns sourceCardFixture().copy(
            sourceId = "sv01-2",
            number = "2",
            name = "Raichu",
        )
        every { cardNameTranslator.translate("Pikachu") } returns "피카츄"
        every { cardNameTranslator.translate("Raichu") } returns "라이츄"
        every { cardRepository.saveAll(capture(cardsSlot)) } answers { firstArg() }

        catalogImporter.importExpansionAndCard(expansionImportCommandFixture())

        with(expansionSlot.captured) {
            seriesId shouldBe series.id
            sourceId shouldBe "sv01"
            name shouldBe Name(en = "Scarlet & Violet", ko = "스칼렛&바이올렛")
            count shouldBe CardCount(total = 258, official = 198)
            image shouldBe ExpansionImage(
                logoUrl = "https://image/sv01.png",
                symbolUrl = "https://image/sv01-symbol.png",
            )
            registeredAt shouldBe TEST_REGISTERED_AT
        }

        cardsSlot.captured.map { it.sourceId } shouldContainExactly listOf("sv01-1", "sv01-2")

        with(cardsSlot.captured.first()) {
            expansionId shouldBe expansionSlot.captured.id
            name shouldBe Name(en = "Pikachu", ko = "피카츄")
            category shouldBe CardCategory.POKEMON
            rarity shouldBe CardRarity.RARE
            illustrator shouldBe "Unknown"
            variants shouldBe CardVariants(holo = true, reverse = true)
            registeredAt shouldBe TEST_REGISTERED_AT
        }

        cardsSlot.captured[1].name shouldBe Name(en = "Raichu", ko = "라이츄")

        verify(exactly = 1) {
            seriesRepository.findBySourceId("sv")
            expansionRepository.existsBySourceId("sv01")
            catalogSourceFetcher.fetchExpansion("sv01")
            expansionRepository.save(any())
            catalogSourceFetcher.fetchCard("sv01-1")
            catalogSourceFetcher.fetchCard("sv01-2")
            cardNameTranslator.translate("Pikachu")
            cardNameTranslator.translate("Raichu")
            cardRepository.saveAll(any())
        }
    }

    @Test
    fun `한글 이름 매핑이 없으면 카드 한글 이름을 비워 등록한다`() {
        val cardsSlot = slot<List<Card>>()

        stubExpansionImport(cardsSlot)

        every { cardNameTranslator.translate("Pikachu") } returns null

        catalogImporter.importExpansionAndCard(expansionImportCommandFixture())

        cardsSlot.captured.single().name shouldBe Name(en = "Pikachu", ko = null)

        verify(exactly = 1) { cardNameTranslator.translate("Pikachu") }
    }

    @Test
    fun `등록되지 않은 시리즈의 확장팩은 등록하지 않는다`() {
        every { seriesRepository.findBySourceId("sv") } returns null

        val exception = shouldThrow<DomainException> {
            catalogImporter.importExpansionAndCard(expansionImportCommandFixture())
        }

        exception.errorCode shouldBe CatalogImportErrorCode.SERIES_NOT_REGISTERED
        verifyNoExpansionOrCardSaved()
    }

    @Test
    fun `확장팩을 선택하지 않으면 등록하지 않는다`() {
        every { seriesRepository.findBySourceId("sv") } returns seriesFixture()

        val exception = shouldThrow<DomainException> {
            catalogImporter.importExpansionAndCard(
                expansionImportCommandFixture(expansions = emptyList()),
            )
        }

        exception.errorCode shouldBe CatalogImportErrorCode.EMPTY_EXPANSION_SELECTION
        verifyNoExpansionOrCardSaved()
    }

    @Test
    fun `같은 확장팩을 중복 선택하면 등록하지 않는다`() {
        val selection = expansionImportSelectionCommandFixture()

        every { seriesRepository.findBySourceId("sv") } returns seriesFixture()

        val exception = shouldThrow<DomainException> {
            catalogImporter.importExpansionAndCard(
                expansionImportCommandFixture(expansions = listOf(selection, selection)),
            )
        }

        exception.errorCode shouldBe CatalogImportErrorCode.DUPLICATE_EXPANSION_SELECTION
        verifyNoExpansionOrCardSaved()
    }

    @Test
    fun `이미 등록된 확장팩은 다시 등록하지 않는다`() {
        every { seriesRepository.findBySourceId("sv") } returns seriesFixture()
        every { expansionRepository.existsBySourceId("sv01") } returns true

        val exception = shouldThrow<DomainException> {
            catalogImporter.importExpansionAndCard(expansionImportCommandFixture())
        }

        exception.errorCode shouldBe CatalogImportErrorCode.EXPANSION_ALREADY_REGISTERED

        verify(exactly = 0) { catalogSourceFetcher.fetchExpansion(any()) }
        verifyNoExpansionOrCardSaved()
    }

    private fun stubExpansionImport(cardsSlot: CapturingSlot<List<Card>>) {
        every { seriesRepository.findBySourceId("sv") } returns seriesFixture()
        every { expansionRepository.existsBySourceId("sv01") } returns false
        every { catalogSourceFetcher.fetchExpansion("sv01") } returns sourceExpansionFixture()
        every { expansionRepository.save(any()) } answers { firstArg() }
        every { catalogSourceFetcher.fetchCard("sv01-1") } returns sourceCardFixture()
        every { cardRepository.saveAll(capture(cardsSlot)) } answers { firstArg() }
    }

    private fun verifyNoExpansionOrCardSaved() {
        verify(exactly = 0) {
            expansionRepository.save(any())
            cardRepository.saveAll(any())
        }
    }
}
