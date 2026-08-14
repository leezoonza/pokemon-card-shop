package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.query

import com.querydsl.jpa.impl.JPAQueryFactory
import com.zoonza.pokemoncardshop.MySqlTestcontainersConfiguration
import com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence.ExpansionJpaRepository
import com.zoonza.pokemoncardshop.catalog.internal.adapter.out.persistence.SeriesJpaRepository
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesExpansionRow
import com.zoonza.pokemoncardshop.catalog.test.fake.expansionFixture
import com.zoonza.pokemoncardshop.catalog.test.fake.seriesFixture
import io.kotest.matchers.collections.shouldContainExactly
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Import(MySqlTestcontainersConfiguration::class)
@ActiveProfiles("test")
@DataJpaTest
class QueryDslCatalogQueryAdapterTests @Autowired constructor(
    private val seriesRepository: SeriesJpaRepository,
    private val expansionRepository: ExpansionJpaRepository,
    entityManager: EntityManager,
) {
    private val now = Instant.parse("2026-08-14T03:00:00Z")
    private val adapter = QueryDslCatalogQueryAdapter(
        clock = Clock.fixed(now, ZoneOffset.UTC),
        jpaQueryFactory = JPAQueryFactory(entityManager),
    )

    @Test
    fun `최신 시리즈와 확장팩 순으로 조회하고 일주일 이내 등록 여부를 표시한다`() {
        val oldSeries = seriesRepository.saveAndFlush(
            seriesFixture(
                sourceId = "swsh",
                name = "Sword & Shield",
                releaseDate = LocalDate.of(2020, 2, 7),
            ),
        )
        val newSeries = seriesRepository.saveAndFlush(
            seriesFixture(
                sourceId = "sv",
                name = "Scarlet & Violet",
                releaseDate = LocalDate.of(2023, 3, 31),
            ),
        )
        val olderExpansion = expansionRepository.saveAndFlush(
            expansionFixture(
                seriesId = newSeries.id,
                sourceId = "sv01",
                name = "Scarlet & Violet",
                releaseDate = LocalDate.of(2023, 3, 31),
                registeredAt = now.minusSeconds(7 * 24 * 60 * 60 + 1),
            ),
        )
        val newerExpansion = expansionRepository.saveAndFlush(
            expansionFixture(
                seriesId = newSeries.id,
                sourceId = "sv02",
                name = "Paldea Evolved",
                releaseDate = LocalDate.of(2023, 6, 9),
                registeredAt = now.minusSeconds(7 * 24 * 60 * 60),
            ),
        )
        val oldSeriesExpansion = expansionRepository.saveAndFlush(
            expansionFixture(
                seriesId = oldSeries.id,
                sourceId = "swsh01",
                name = "Sword & Shield Base",
                releaseDate = LocalDate.of(2020, 2, 7),
                registeredAt = now.minusSeconds(30 * 24 * 60 * 60),
            ),
        )

        val result = adapter.findAllExpansionRows()

        result shouldContainExactly listOf(
            SeriesExpansionRow(
                newSeries.id,
                "Scarlet & Violet",
                newerExpansion.id,
                "Paldea Evolved",
                "https://image/sv01.png",
                LocalDate.of(2023, 6, 9),
                true,
            ),
            SeriesExpansionRow(
                newSeries.id,
                "Scarlet & Violet",
                olderExpansion.id,
                "Scarlet & Violet",
                "https://image/sv01.png",
                LocalDate.of(2023, 3, 31),
                false,
            ),
            SeriesExpansionRow(
                oldSeries.id,
                "Sword & Shield",
                oldSeriesExpansion.id,
                "Sword & Shield Base",
                "https://image/sv01.png",
                LocalDate.of(2020, 2, 7),
                false,
            ),
        )
    }
}
