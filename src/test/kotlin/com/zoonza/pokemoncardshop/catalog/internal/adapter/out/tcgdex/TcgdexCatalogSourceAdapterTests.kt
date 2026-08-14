package com.zoonza.pokemoncardshop.catalog.internal.adapter.out.tcgdex

import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import net.tcgdex.sdk.Extension
import net.tcgdex.sdk.Quality
import net.tcgdex.sdk.TCGdex
import net.tcgdex.sdk.models.*
import net.tcgdex.sdk.models.subs.SetCardCount
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.LocalDate
import net.tcgdex.sdk.models.Set as TcgdexSet
import net.tcgdex.sdk.models.subs.CardVariants as TcgdexCardVariants

class TcgdexCatalogSourceAdapterTests {

    private val client = mockk<TCGdex>()
    private val adapter = TcgdexCatalogSourceAdapter(client)

    @Test
    fun `TCGdex 시리즈 목록을 내부 요약으로 변환한다`() {
        val series = mockk<SerieResume>()
        every { client.fetchSeries() } returns arrayOf(series)
        every { series.id } returns "sv"
        every { series.name } returns "Scarlet & Violet"
        every { series.getLogoUrl(Extension.PNG) } returns "https://image/sv.png"

        val result = adapter.fetchSeriesSummaries()

        result.single().sourceId shouldBe "sv"
        result.single().name shouldBe "Scarlet & Violet"
        result.single().logoUrl shouldBe "https://image/sv.png"
    }

    @Test
    fun `TCGdex 시리즈와 확장팩 요약을 내부 데이터로 변환한다`() {
        val series = mockk<Serie>()
        val expansion = mockk<SetResume>()
        every { client.fetchSerie("sv") } returns series
        every { series.id } returns "sv"
        every { series.name } returns "Scarlet & Violet"
        every { series.sets } returns listOf(expansion)
        every { expansion.id } returns "sv01"
        every { expansion.name } returns "Scarlet & Violet"
        every { expansion.getLogoUrl(Extension.PNG) } returns "https://image/sv01.png"
        every { expansion.getSymbolUrl(Extension.PNG) } returns null

        val result = adapter.fetchSeries("sv")

        result.sourceId shouldBe "sv"
        result.expansions.single().sourceId shouldBe "sv01"
        result.expansions.single().symbolUrl shouldBe null
    }

    @Test
    fun `TCGdex 확장팩을 내부 데이터로 변환한다`() {
        val set = mockk<TcgdexSet>()
        val count = mockk<SetCardCount>()
        val card = mockk<CardResume>()
        every { client.fetchSet("sv01") } returns set
        every { set.id } returns "sv01"
        every { set.name } returns "Scarlet & Violet"
        every { set.getLogoUrl(Extension.PNG) } returns "https://image/sv01.png"
        every { set.getSymbolUrl(Extension.PNG) } returns null
        every { set.releaseDate } returns "2023-03-31"
        every { set.cardCount } returns count
        every { set.cards } returns listOf(card)
        every { count.total } returns 258
        every { count.official } returns 198
        every { card.id } returns "sv01-1"

        val result = adapter.fetchExpansion("sv01")

        result.sourceId shouldBe "sv01"
        result.releaseDate shouldBe LocalDate.of(2023, 3, 31)
        result.totalCardCount shouldBe 258
        result.symbolUrl shouldBe null
        result.cardSourceIds shouldContainExactly listOf("sv01-1")
    }

    @Test
    fun `TCGdex 카드를 내부 데이터로 변환한다`() {
        val card = mockk<Card>()
        val variants = mockk<TcgdexCardVariants>()
        every { client.fetchCard("sv01-1") } returns card
        every { card.id } returns "sv01-1"
        every { card.category } returns "Pokemon"
        every { card.localId } returns "1"
        every { card.name } returns "Pikachu"
        every { card.image } returns "https://image/sv01-1"
        every { card.getImageUrl(Quality.HIGH, Extension.WEBP) } returns
                "https://image/sv01-1/high.webp"
        every { card.illustrator } returns null
        every { card.rarity } returns "Rare"
        every { card.variants } returns variants
        every { variants.firstEdition } returns null
        every { variants.holo } returns true
        every { variants.normal } returns false
        every { variants.reverse } returns true
        every { variants.wPromo } returns null

        val result = adapter.fetchCard("sv01-1")

        result.localId shouldBe "1"
        result.imageUrl shouldBe "https://image/sv01-1/high.webp"
        result.illustrator shouldBe null
        result.variants.firstEdition shouldBe false
        result.variants.holo shouldBe true
        result.variants.reverse shouldBe true
    }

    @Test
    fun `TCGdex에 데이터가 없으면 찾을 수 없음 오류로 변환한다`() {
        every { client.fetchSerie("missing") } returns null

        val exception = shouldThrow<DomainException> {
            adapter.fetchSeries("missing")
        }

        exception.errorCode shouldBe CatalogImportErrorCode.SOURCE_DATA_NOT_FOUND
    }

    @Test
    fun `TCGdex 조회 실패는 일시적인 외부 오류로 변환한다`() {
        every { client.fetchSerie("sv") } throws IOException("connection failed")

        val exception = shouldThrow<DomainException> {
            adapter.fetchSeries("sv")
        }

        exception.errorCode shouldBe CatalogImportErrorCode.SOURCE_UNAVAILABLE
    }
}
