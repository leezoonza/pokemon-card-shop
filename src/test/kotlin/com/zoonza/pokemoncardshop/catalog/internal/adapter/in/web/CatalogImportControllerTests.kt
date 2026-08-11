package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportCommand
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportResult
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionCandidateSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesCandidateSummary
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.CatalogCandidateFinder
import com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`.ImportCatalogUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

class CatalogImportControllerTests {

    private val catalogCandidateFinder = mockk<CatalogCandidateFinder>()
    private val importCatalog = mockk<ImportCatalogUseCase>()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(
            CatalogImportController(
                catalogCandidateFinder = catalogCandidateFinder,
                importCatalogUseCase = importCatalog,
            ),
        )
        .build()

    @Test
    fun `시리즈 등록 후보를 응답한다`() {
        every { catalogCandidateFinder.findSeries() } returns listOf(
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

        verify(exactly = 1) { catalogCandidateFinder.findSeries() }
    }

    @Test
    fun `선택한 시리즈의 확장팩 등록 후보를 응답한다`() {
        every { catalogCandidateFinder.findExpansions("sv") } returns listOf(
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

        verify(exactly = 1) { catalogCandidateFinder.findExpansions("sv") }
    }

    @Test
    fun `시리즈와 선택한 확장팩을 등록한다`() {
        val command = CatalogImportCommand(
            seriesSourceId = "sv",
            seriesReleaseDate = LocalDate.of(2023, 3, 31),
            expansionSourceIds = listOf("sv01", "sv02"),
        )
        every { importCatalog.importCatalog(command) } returns CatalogImportResult(
            seriesId = 1L,
            expansionCount = 2,
            cardCount = 500,
        )

        mockMvc.post("/api/admin/catalog/imports") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "seriesSourceId": "sv",
                  "seriesReleaseDate": "2023-03-31",
                  "expansionSourceIds": ["sv01", "sv02"]
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.seriesId") { value(1) }
            jsonPath("$.data.expansionCount") { value(2) }
            jsonPath("$.data.cardCount") { value(500) }
        }

        verify(exactly = 1) { importCatalog.importCatalog(command) }
    }
}
