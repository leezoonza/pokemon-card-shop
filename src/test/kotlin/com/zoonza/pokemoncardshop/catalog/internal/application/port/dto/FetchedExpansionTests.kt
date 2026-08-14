package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.CardCount
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionImage
import com.zoonza.pokemoncardshop.catalog.test.fake.TEST_REGISTERED_AT
import com.zoonza.pokemoncardshop.catalog.test.fake.sourceExpansionFixture
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FetchedExpansionTests {
    @Test
    fun `조회한 확장팩을 등록 정보로 변환한다`() {
        val info = fetchedExpansion().toExpansionRegisterInfo(
            seriesId = 1L,
            registeredAt = TEST_REGISTERED_AT,
        )

        info.seriesId shouldBe 1L
        info.sourceId shouldBe "sv01"
        info.name shouldBe "Scarlet & Violet"
        info.count shouldBe CardCount(total = 258, official = 198)
        info.image shouldBe ExpansionImage(
            logoUrl = "https://image/sv01.png",
            symbolUrl = "https://image/sv01-symbol.png",
        )
        info.releaseDate shouldBe LocalDate.of(2023, 3, 31)
        info.registeredAt shouldBe TEST_REGISTERED_AT
    }

    private fun fetchedExpansion(
        source: SourceExpansion = sourceExpansionFixture(),
    ): FetchedExpansion = FetchedExpansion(
        source = source,
        cards = emptyList(),
    )
}
