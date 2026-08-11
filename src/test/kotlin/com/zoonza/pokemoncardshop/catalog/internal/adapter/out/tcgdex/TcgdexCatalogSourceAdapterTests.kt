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
import net.tcgdex.sdk.models.Card
import net.tcgdex.sdk.models.CardResume
import net.tcgdex.sdk.models.SerieResume
import net.tcgdex.sdk.models.SetResume
import net.tcgdex.sdk.models.subs.SetCardCount
import org.junit.jupiter.api.Test
import java.time.LocalDate
import net.tcgdex.sdk.models.Set as TcgdexSet
import net.tcgdex.sdk.models.subs.CardVariants as TcgdexCardVariants

class TcgdexCatalogSourceAdapterTests {

    private val client = mockk<TCGdex>()
    private val adapter = TcgdexCatalogSourceAdapter(client)

    @Test
    fun `TCGdex 확장팩을 내부 데이터로 변환한다`() {
        val set = mockk<TcgdexSet>()
        val series = mockk<SerieResume>()
        val count = mockk<SetCardCount>()
        val card = mockk<CardResume>()
        every { client.fetchSet("sv01") } returns set
        every { set.id } returns "sv01"
        every { set.name } returns "Scarlet & Violet"
        every { set.serie } returns series
        every { set.getLogoUrl(Extension.PNG) } returns "https://image/sv01.png"
        every { set.getSymbolUrl(Extension.PNG) } returns null
        every { set.releaseDate } returns "2023-03-31"
        every { set.cardCount } returns count
        every { set.cards } returns listOf(card)
        every { series.id } returns "sv"
        every { count.total } returns 258
        every { count.official } returns 198
        every { card.id } returns "sv01-1"

        val result = adapter.getExpansion("sv01")

        result.sourceId shouldBe "sv01"
        result.seriesSourceId shouldBe "sv"
        result.releaseDate shouldBe LocalDate.of(2023, 3, 31)
        result.totalCardCount shouldBe 258
        result.symbolUrl shouldBe null
        result.cardSourceIds shouldContainExactly listOf("sv01-1")
    }

    @Test
    fun `TCGdex 카드를 내부 데이터로 변환한다`() {
        val card = mockk<Card>()
        val set = mockk<SetResume>()
        val variants = mockk<TcgdexCardVariants>()
        every { client.fetchCard("sv01-1") } returns card
        every { card.id } returns "sv01-1"
        every { card.set } returns set
        every { set.id } returns "sv01"
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

        val result = adapter.getCard("sv01-1")

        result.expansionSourceId shouldBe "sv01"
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
}
