package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionCandidateSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesCandidateSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogFetchUseCase
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogImportUseCase
import com.zoonza.pokemoncardshop.catalog.test.fake.expansionImportCommandFixture
import com.zoonza.pokemoncardshop.catalog.test.fake.expansionImportSelectionCommandFixture
import com.zoonza.pokemoncardshop.catalog.test.fake.seriesImportCommandFixture
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class CatalogImportControllerTests {

    private val catalogFetchUseCase = mockk<CatalogFetchUseCase>()
    private val catalogImportUseCase = mockk<CatalogImportUseCase>()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(
            CatalogImportController(
                catalogFetchUseCase = catalogFetchUseCase,
                catalogImportUseCase = catalogImportUseCase,
            ),
        )
        .build()

    @Test
    fun `시리즈 등록 후보를 응답한다`() {
        every { catalogFetchUseCase.fetchSeriesSummaries() } returns listOf(
            SeriesCandidateSummary(
                sourceId = "sv",
                name = "Scarlet & Violet",
                logoUrl = "https://image/sv.png",
                registered = false,
            ),
        )

        mockMvc.get("/api/admin/catalog/imports/series")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[0].sourceId") { value("sv") }
                jsonPath("$.data[0].logoUrl") { value("https://image/sv.png") }
                jsonPath("$.data[0].registered") { value(false) }
            }

        verify(exactly = 1) { catalogFetchUseCase.fetchSeriesSummaries() }
    }

    @Test
    fun `시리즈 등록 요청을 전달한다`() {
        val command = seriesImportCommandFixture()
        every { catalogImportUseCase.importSeries(command) } just Runs

        mockMvc.post("/api/admin/catalog/imports/series") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "seriesSourceId": "sv",
                  "seriesNameKo": "스칼렛&바이올렛",
                  "seriesReleaseDate": "2023-03-31"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
        }

        verify(exactly = 1) { catalogImportUseCase.importSeries(command) }
    }

    @Test
    fun `선택한 시리즈의 확장팩 등록 후보를 응답한다`() {
        every { catalogFetchUseCase.fetchExpansionSummaries("sv") } returns listOf(
            ExpansionCandidateSummary(
                sourceId = "sv01",
                name = "Scarlet & Violet",
                logoUrl = "https://image/sv01.png",
                symbolUrl = null,
                registered = false,
            ),
        )

        mockMvc.get("/api/admin/catalog/imports/series/sv/expansions")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data[0].sourceId") { value("sv01") }
                jsonPath("$.data[0].symbolUrl") { doesNotExist() }
            }

        verify(exactly = 1) { catalogFetchUseCase.fetchExpansionSummaries("sv") }
    }

    @Test
    fun `선택한 확장팩과 카드 등록 요청을 전달한다`() {
        val command = expansionImportCommandFixture(
            expansions = listOf(
                expansionImportSelectionCommandFixture(),
                expansionImportSelectionCommandFixture(
                    expansionSourceId = "sv02",
                    expansionNameKo = "트리플렛비트",
                ),
            ),
        )
        every { catalogImportUseCase.importExpansionAndCard(command) } just Runs

        mockMvc.post("/api/admin/catalog/imports/series/sv/expansions") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "expansions": [
                    {
                      "expansionSourceId": "sv01",
                      "expansionNameKo": "스칼렛&바이올렛"
                    },
                    {
                      "expansionSourceId": "sv02",
                      "expansionNameKo": "트리플렛비트"
                    }
                  ]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
        }

        verify(exactly = 1) { catalogImportUseCase.importExpansionAndCard(command) }
    }

    @Test
    fun `확장팩을 선택하지 않으면 요청을 거절한다`() {
        mockMvc.post("/api/admin/catalog/imports/series/sv/expansions") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"expansions": []}"""
        }.andExpect {
            status { isBadRequest() }
        }

        verify(exactly = 0) { catalogImportUseCase.importExpansionAndCard(any()) }
    }
}
