package com.zoonza.pokemoncardshop.catalog.internal.application.service

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.*
import com.zoonza.pokemoncardshop.catalog.internal.domain.*
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class CatalogRegistrationServiceTests {

    private val seriesRepository = mockk<SeriesRepository>()
    private val expansionRepository = mockk<ExpansionRepository>()
    private val cardRepository = mockk<CardRepository>()
    private val registeredAt = Instant.parse("2026-08-07T03:00:00Z")
    private val service = CatalogRegistrationService(
        seriesRepository = seriesRepository,
        expansionRepository = expansionRepository,
        cardRepository = cardRepository,
        clock = Clock.fixed(registeredAt, ZoneOffset.UTC),
    )

    @Test
    fun `준비된 카탈로그를 같은 등록 시각으로 저장한다`() {
        val seriesSlot = slot<Series>()
        val expansionSlot = slot<Expansion>()
        val cardsSlot = slot<List<Card>>()
        every { seriesRepository.findBySourceId("sv") } returns null
        every { seriesRepository.save(capture(seriesSlot)) } answers { firstArg() }
        every { expansionRepository.existsBySourceId("sv01") } returns false
        every { expansionRepository.save(capture(expansionSlot)) } answers { firstArg() }
        every { cardRepository.saveAll(capture(cardsSlot)) } answers { firstArg() }

        val result = service.register(plan())

        result shouldBe CatalogImportResult(seriesId = 0L, expansionCount = 1, cardCount = 1)
        with(seriesSlot.captured) {
            sourceId shouldBe "sv"
            releaseDate shouldBe LocalDate.of(2023, 3, 31)
            this.registeredAt shouldBe registeredAt
        }
        with(expansionSlot.captured) {
            sourceId shouldBe "sv01"
            count shouldBe CardCount(total = 258, official = 198)
            image shouldBe ExpansionImage(
                logoUrl = "https://image/sv01.png",
                symbolUrl = "",
            )
            this.registeredAt shouldBe registeredAt
        }
        with(cardsSlot.captured.single()) {
            sourceId shouldBe "sv01-1"
            category shouldBe CardCategory.POKEMON
            rarity shouldBe CardRarity.RARE
            variants shouldBe CardVariants(holo = true, reverse = true)
            this.registeredAt shouldBe registeredAt
        }
        verify(exactly = 1) {
            seriesRepository.save(any())
            expansionRepository.save(any())
            cardRepository.saveAll(any())
        }
    }

    @Test
    fun `기존 시리즈에는 시리즈를 다시 저장하지 않고 확장팩을 추가한다`() {
        val series = Series.register(
            sourceId = "sv",
            name = "Scarlet & Violet",
            releaseDate = LocalDate.of(2023, 3, 31),
            registeredAt = Instant.EPOCH,
        )
        every { seriesRepository.findBySourceId("sv") } returns series
        every { expansionRepository.existsBySourceId("sv01") } returns false
        every { expansionRepository.save(any()) } answers { firstArg() }
        every { cardRepository.saveAll(any()) } answers { firstArg() }

        service.register(plan())

        verify(exactly = 0) { seriesRepository.save(any()) }
        verify(exactly = 1) {
            expansionRepository.save(any())
            cardRepository.saveAll(any())
        }
    }

    private fun plan() = CatalogRegistrationPlan(
        series = SeriesRegistrationData(
            sourceId = "sv",
            name = "Scarlet & Violet",
            releaseDate = LocalDate.of(2023, 3, 31),
        ),
        expansions = listOf(
            ExpansionRegistrationData(
                sourceId = "sv01",
                name = "Scarlet & Violet",
                totalCardCount = 258,
                officialCardCount = 198,
                logoUrl = "https://image/sv01.png",
                symbolUrl = null,
                releaseDate = LocalDate.of(2023, 3, 31),
                cards = listOf(
                    CardRegistrationData(
                        sourceId = "sv01-1",
                        category = CardCategory.POKEMON,
                        number = "1",
                        name = "Pikachu",
                        imageUrl = "https://image/sv01-1/high.webp",
                        illustrator = "Unknown",
                        rarity = CardRarity.RARE,
                        variants = SourceCardVariants(
                            firstEdition = false,
                            holo = true,
                            normal = false,
                            reverse = true,
                            wPromo = false,
                        ),
                    ),
                ),
            ),
        ),
    )
}
