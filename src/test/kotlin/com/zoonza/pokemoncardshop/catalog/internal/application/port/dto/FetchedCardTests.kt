package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardCategory
import com.zoonza.pokemoncardshop.catalog.internal.domain.card.CardRarity
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.catalog.test.fake.TEST_REGISTERED_AT
import com.zoonza.pokemoncardshop.catalog.test.fake.sourceCardFixture
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FetchedCardTests {

    @Test
    fun `조회한 카드를 등록 정보로 변환한다`() {
        val info = fetchedCard(sourceCardFixture()).toCardRegisterInfo(
            expansionId = 1L,
            registeredAt = TEST_REGISTERED_AT,
        )

        info.expansionId shouldBe 1L
        info.name shouldBe Name(en = "Pikachu", ko = "피카츄")
        info.category shouldBe CardCategory.POKEMON
        info.rarity shouldBe CardRarity.RARE
    }

    @Test
    fun `이미지가 없어도 카드 등록 정보로 변환한다`() {
        val info = fetchedCard(sourceCardFixture().copy(imageUrl = null)).toCardRegisterInfo(
            expansionId = 1L,
            registeredAt = TEST_REGISTERED_AT,
        )

        info.imageUrl shouldBe null
    }

    private fun fetchedCard(
        source: SourceCard,
        nameKo: String? = "피카츄",
    ): FetchedCard = FetchedCard(source, nameKo)
}
