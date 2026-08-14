package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CardItem
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionItem
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesWithExpansions
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogQueryUseCase
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRarity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

class CatalogControllerTests {

    private val catalogQueryUseCase = mockk<CatalogQueryUseCase>()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(CatalogController(catalogQueryUseCase))
        .build()

    @Test
    fun `여러 시리즈의 확장팩 목록을 입력 순서대로 응답한다`() {
        every { catalogQueryUseCase.getSeriesWithExpansions() } returns listOf(
            SeriesWithExpansions(
                seriesId = 1L,
                seriesName = "Scarlet & Violet",
                expansions = listOf(
                    ExpansionItem(
                        expansionId = 2L,
                        name = "Paldea Evolved",
                        logoUrl = "https://image/sv02.png",
                        releaseDate = LocalDate.of(2023, 6, 9),
                        isNew = true,
                    ),
                    ExpansionItem(
                        expansionId = 1L,
                        name = "Scarlet & Violet",
                        logoUrl = "https://image/sv01.png",
                        releaseDate = LocalDate.of(2023, 3, 31),
                        isNew = false,
                    ),
                ),
            ),
            SeriesWithExpansions(
                seriesId = 2L,
                seriesName = "Sword & Shield",
                expansions = listOf(
                    ExpansionItem(
                        expansionId = 3L,
                        name = "Silver Tempest",
                        logoUrl = "https://image/swsh12.png",
                        releaseDate = LocalDate.of(2022, 11, 11),
                        isNew = false,
                    ),
                ),
            ),
        )

        mockMvc.get("/api/catalog/expansions")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.length()") { value(2) }
                jsonPath("$.data[0].seriesId") { value(1) }
                jsonPath("$.data[0].seriesName") { value("Scarlet & Violet") }
                jsonPath("$.data[0].expansions.length()") { value(2) }
                jsonPath("$.data[0].expansions[0].expansionId") { value(2) }
                jsonPath("$.data[0].expansions[0].name") { value("Paldea Evolved") }
                jsonPath("$.data[0].expansions[0].logoUrl") { value("https://image/sv02.png") }
                jsonPath("$.data[0].expansions[0].releaseDate") { value("2023-06-09") }
                jsonPath("$.data[0].expansions[0].isNew") { value(true) }
                jsonPath("$.data[0].expansions[1].expansionId") { value(1) }
                jsonPath("$.data[0].expansions[1].name") { value("Scarlet & Violet") }
                jsonPath("$.data[0].expansions[1].logoUrl") { value("https://image/sv01.png") }
                jsonPath("$.data[0].expansions[1].releaseDate") { value("2023-03-31") }
                jsonPath("$.data[0].expansions[1].isNew") { value(false) }
                jsonPath("$.data[1].seriesId") { value(2) }
                jsonPath("$.data[1].seriesName") { value("Sword & Shield") }
                jsonPath("$.data[1].expansions.length()") { value(1) }
                jsonPath("$.data[1].expansions[0].expansionId") { value(3) }
                jsonPath("$.data[1].expansions[0].name") { value("Silver Tempest") }
                jsonPath("$.data[1].expansions[0].logoUrl") { value("https://image/swsh12.png") }
                jsonPath("$.data[1].expansions[0].releaseDate") { value("2022-11-11") }
                jsonPath("$.data[1].expansions[0].isNew") { value(false) }
            }

        verify(exactly = 1) { catalogQueryUseCase.getSeriesWithExpansions() }
    }

    @Test
    fun `조회 결과가 없으면 빈 목록을 응답한다`() {
        every { catalogQueryUseCase.getSeriesWithExpansions() } returns emptyList()

        mockMvc.get("/api/catalog/expansions")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data") { isEmpty() }
            }

        verify(exactly = 1) { catalogQueryUseCase.getSeriesWithExpansions() }
    }

    @Test
    fun `확장팩의 카드 목록을 응답한다`() {
        every { catalogQueryUseCase.getCards(10L) } returns listOf(
            CardItem(
                cardId = 1L,
                nameEn = "Pikachu",
                nameKo = "피카츄",
                rarity = CardRarity.RARE,
                imageUrl = "https://image/sv01-1/high.webp",
                printNumber = "1/198",
            ),
            CardItem(
                cardId = 2L,
                nameEn = "Raichu",
                nameKo = null,
                rarity = CardRarity.UNCOMMON,
                imageUrl = null,
                printNumber = "2/198",
            ),
        )

        mockMvc.get("/api/catalog/expansion/10")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.length()") { value(2) }
                jsonPath("$.data[0].cardId") { value(1) }
                jsonPath("$.data[0].nameEn") { value("Pikachu") }
                jsonPath("$.data[0].nameKo") { value("피카츄") }
                jsonPath("$.data[0].rarity") { value("Rare") }
                jsonPath("$.data[0].imageUrl") { value("https://image/sv01-1/high.webp") }
                jsonPath("$.data[0].printNumber") { value("1/198") }
                jsonPath("$.data[1].cardId") { value(2) }
                jsonPath("$.data[1].nameEn") { value("Raichu") }
                jsonPath("$.data[1].nameKo") { isEmpty() }
                jsonPath("$.data[1].rarity") { value("Uncommon") }
                jsonPath("$.data[1].imageUrl") { isEmpty() }
                jsonPath("$.data[1].printNumber") { value("2/198") }
            }

        verify(exactly = 1) { catalogQueryUseCase.getCards(10L) }
    }
}
