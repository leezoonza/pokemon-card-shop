package com.zoonza.pokemoncardshop.catalog.internal.domain

import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CardCountTests {

    @Test
    fun `전체 카드 수와 공식 카드 수가 같으면 생성한다`() {
        val cardCount = CardCount(total = 10, official = 10)

        cardCount shouldBe CardCount(total = 10, official = 10)
    }

    @Test
    fun `전체 카드 수가 음수이면 거절한다`() {
        assertInvalidCardCount(
            total = -1,
            official = 0,
            expectedErrorCode = ExpansionErrorCode.NEGATIVE_TOTAL_CARD_COUNT,
        )
    }

    @Test
    fun `공식 카드 수가 음수이면 거절한다`() {
        assertInvalidCardCount(
            total = 0,
            official = -1,
            expectedErrorCode = ExpansionErrorCode.NEGATIVE_OFFICIAL_CARD_COUNT,
        )
    }

    @Test
    fun `공식 카드 수가 전체 카드 수보다 많으면 거절한다`() {
        assertInvalidCardCount(
            total = 10,
            official = 11,
            expectedErrorCode = ExpansionErrorCode.OFFICIAL_CARD_COUNT_EXCEEDS_TOTAL,
        )
    }

    private fun assertInvalidCardCount(
        total: Int,
        official: Int,
        expectedErrorCode: ExpansionErrorCode,
    ) {
        val exception = shouldThrow<DomainException> {
            CardCount(total = total, official = official)
        }

        exception.errorCode shouldBe expectedErrorCode
    }
}
