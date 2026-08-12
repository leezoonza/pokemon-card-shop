package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CardCategoryTests {

    @Test
    fun `지원하는 모든 카테고리를 원본 값으로 찾는다`() {
        CardCategory.entries.forEach { category ->
            CardCategory.from(category.value) shouldBe category
        }
    }

    @Test
    fun `지원하지 않는 카테고리는 거절한다`() {
        val exception = shouldThrow<DomainException> {
            CardCategory.from("Unknown")
        }

        exception.errorCode shouldBe CardErrorCode.NOT_SUPPORTED_CATEGORY
    }
}
