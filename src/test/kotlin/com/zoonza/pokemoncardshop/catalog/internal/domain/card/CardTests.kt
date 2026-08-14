package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.catalog.test.fake.cardRegisterInfoFixture
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CardTests {
    @Test
    fun `영문 이름이 비어 있으면 카드를 등록하지 않는다`() {
        val exception = shouldThrow<DomainException> {
            Card.register(
                cardRegisterInfoFixture().copy(
                    category = CardCategory.POKEMON,
                    name = Name(en = "", ko = null),
                ),
            )
        }

        exception.errorCode shouldBe CardErrorCode.ENGLISH_NAME_REQUIRED
    }
}
