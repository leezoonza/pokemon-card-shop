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
import net.tcgdex.sdk.models.subs.CardAbility
import net.tcgdex.sdk.models.subs.CardAttack
import net.tcgdex.sdk.models.subs.CardWeakRes
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
        every { series.getLogoUrl(Extension.PNG) } returns "https://image/sv.png"
        every { series.sets } returns listOf(expansion)
        every { expansion.id } returns "sv01"
        every { expansion.name } returns "Scarlet & Violet"
        every { expansion.getLogoUrl(Extension.PNG) } returns "https://image/sv01.png"
        every { expansion.getSymbolUrl(Extension.PNG) } returns null

        val result = adapter.fetchSeries("sv")

        result.sourceId shouldBe "sv"
        result.logoUrl shouldBe "https://image/sv.png"
        result.expansions.single().sourceId shouldBe "sv01"
        result.expansions.single().symbolUrl shouldBe null
    }

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

        val result = adapter.fetchExpansion("sv01")

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
        val ability = mockk<CardAbility>()
        val attack = mockk<CardAttack>()
        val weakness = mockk<CardWeakRes>()
        val resistance = mockk<CardWeakRes>()
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
        every { card.abilities } returns listOf(ability)
        every { ability.type } returns "Ability"
        every { ability.name } returns "Static Shock"
        every { ability.effect } returns "상대 포켓몬을 마비시킨다."
        every { card.dexId } returns listOf(25)
        every { card.hp } returns 60
        every { card.types } returns listOf("Lightning")
        every { card.evolveFrom } returns null
        every { card.description } returns "전기를 모아 공격한다."
        every { card.stage } returns "Basic"
        every { card.suffix } returns null
        every { card.attacks } returns listOf(attack)
        every { attack.name } returns "Thunder Shock"
        every { attack.cost } returns listOf("Lightning")
        every { attack.effect } returns null
        every { attack.damage } returns "30"
        every { card.weaknesses } returns listOf(weakness)
        every { weakness.type } returns "Fighting"
        every { weakness.value } returns "x2"
        every { card.resistances } returns listOf(resistance)
        every { resistance.type } returns "Metal"
        every { resistance.value } returns "-30"
        every { card.retreat } returns 1
        every { card.effect } returns null
        every { card.trainerType } returns null
        every { card.energyType } returns null

        val result = adapter.fetchCard("sv01-1")

        result.expansionSourceId shouldBe "sv01"
        result.imageUrl shouldBe "https://image/sv01-1/high.webp"
        result.illustrator shouldBe null
        result.variants.firstEdition shouldBe false
        result.variants.holo shouldBe true
        result.variants.reverse shouldBe true
        result.abilities.single().name shouldBe "Static Shock"
        result.dexIds shouldContainExactly listOf(25)
        result.hp shouldBe 60
        result.types shouldContainExactly listOf("Lightning")
        result.attacks.single().damage shouldBe "30"
        result.weaknesses.single().value shouldBe "x2"
        result.resistances.single().value shouldBe "-30"
        result.retreat shouldBe 1
        result.effect shouldBe null
        result.trainerType shouldBe null
        result.energyType shouldBe null
    }

    @Test
    fun `TCGdex 카드의 선택 목록이 생략되면 빈 목록으로 변환한다`() {
        val card = deserializeTcgdexCard(
            """
            {
              "id": "swsh3-136",
              "localId": "136",
              "name": "Furret",
              "rarity": "Uncommon",
              "category": "Pokemon",
              "set": { "id": "swsh3" }
            }
            """.trimIndent(),
        )
        every { client.fetchCard("swsh3-136") } returns card

        val result = adapter.fetchCard("swsh3-136")

        result.abilities shouldBe emptyList()
        result.attacks shouldBe emptyList()
        result.weaknesses shouldBe emptyList()
        result.resistances shouldBe emptyList()
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

    private fun deserializeTcgdexCard(json: String): Card {
        val gsonClass = Class.forName("com.google.gson.Gson")
        val gson = gsonClass.getDeclaredConstructor().newInstance()
        return gsonClass
            .getMethod("fromJson", String::class.java, Class::class.java)
            .invoke(gson, json, Card::class.java) as Card
    }
}
