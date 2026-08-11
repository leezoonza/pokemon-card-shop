package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.MySqlTestcontainersConfiguration
import com.zoonza.pokemoncardshop.catalog.internal.domain.*
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.Card
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardCategory
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRarity
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardVariants
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.CardCount
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.Expansion
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionImage
import com.zoonza.pokemoncardshop.catalog.internal.domain.series.Series
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.time.LocalDate

@Import(MySqlTestcontainersConfiguration::class)
@ActiveProfiles("test")
@DataJpaTest
class CatalogJpaRepositoryTests @Autowired constructor(
    private val seriesRepository: SeriesJpaRepository,
    private val expansionRepository: ExpansionJpaRepository,
    private val cardRepository: CardJpaRepository,
    private val entityManager: EntityManager,
) {

    @Test
    fun `시리즈와 확장팩과 카드를 저장하고 조회한다`() {
        val registeredAt = Instant.parse("2026-08-07T03:00:00Z")
        val series = seriesRepository.saveAndFlush(
            Series.register(
                sourceId = "sv",
                name = "Scarlet & Violet",
                releaseDate = LocalDate.of(2023, 3, 31),
                registeredAt = registeredAt,
            ),
        )
        val expansion = expansionRepository.saveAndFlush(
            Expansion.register(
                seriesId = series.id,
                sourceId = "sv01",
                name = "Scarlet & Violet Base",
                count = CardCount(total = 258, official = 198),
                image = ExpansionImage(
                    logoUrl = "https://image/sv01.png",
                    symbolUrl = "",
                ),
                releaseDate = LocalDate.of(2023, 3, 31),
                registeredAt = registeredAt,
            ),
        )
        val card = cardRepository.saveAndFlush(
            Card.register(
                expansionId = expansion.id,
                sourceId = "sv01-1",
                category = CardCategory.POKEMON,
                localId = "1",
                name = "Pikachu",
                imageUrl = "https://image/sv01-1/high.webp",
                illustrator = "Unknown",
                rarity = CardRarity.RARE,
                variants = CardVariants(holo = true, reverse = true),
                registeredAt = registeredAt,
            ),
        )

        entityManager.clear()

        val foundSeries = seriesRepository.findBySourceId("sv")
        val foundExpansion = expansionRepository.findById(expansion.id).orElseThrow()
        val foundCard = cardRepository.findById(card.id).orElseThrow()

        series.id shouldBeGreaterThan 0L
        expansion.id shouldBeGreaterThan 0L
        card.id shouldBeGreaterThan 0L
        foundSeries?.releaseDate shouldBe LocalDate.of(2023, 3, 31)
        foundExpansion.seriesId shouldBe series.id
        foundExpansion.image shouldBe ExpansionImage("https://image/sv01.png", "")
        foundCard.expansionId shouldBe expansion.id
        foundCard.variants shouldBe CardVariants(holo = true, reverse = true)
    }
}
