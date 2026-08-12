package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CardRarityTests {

    @Test
    fun `지원하는 모든 희귀도를 원본 값으로 찾는다`() {
        CardRarity.entries.forEach { rarity ->
            CardRarity.from(rarity.value) shouldBe rarity
        }
    }

    @Test
    fun `지원하지 않는 희귀도는 거절한다`() {
        val exception = shouldThrow<DomainException> {
            CardRarity.from("Unknown Rare")
        }

        exception.errorCode shouldBe CardErrorCode.NOT_SUPPORTED_RARITY
    }
}
