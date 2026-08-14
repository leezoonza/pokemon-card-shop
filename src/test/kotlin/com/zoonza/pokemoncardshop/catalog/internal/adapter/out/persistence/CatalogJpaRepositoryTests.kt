package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence

import com.zoonza.pokemoncardshop.MySqlTestcontainersConfiguration
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardVariants
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionImage
import com.zoonza.pokemoncardshop.catalog.test.fake.cardFixture
import com.zoonza.pokemoncardshop.catalog.test.fake.cardRegisterInfoFixture
import com.zoonza.pokemoncardshop.catalog.test.fake.expansionFixture
import com.zoonza.pokemoncardshop.catalog.test.fake.seriesFixture
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
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
        val series = seriesRepository.saveAndFlush(seriesFixture())
        val expansion = expansionRepository.saveAndFlush(
            expansionFixture(
                seriesId = series.id,
                image = ExpansionImage(
                    logoUrl = "https://image/sv01.png",
                    symbolUrl = "",
                ),
            ),
        )
        val card = cardRepository.saveAndFlush(
            cardFixture(
                cardRegisterInfoFixture().copy(
                    expansionId = expansion.id,
                    imageUrl = null,
                ),
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
        foundCard.imageUrl shouldBe null
        foundCard.variants shouldBe CardVariants(holo = true, reverse = true)
    }
}
