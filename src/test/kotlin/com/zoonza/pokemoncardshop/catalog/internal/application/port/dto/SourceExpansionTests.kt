package com.zoonza.pokemoncardshop.catalog.internal.application.port.dto

import com.zoonza.pokemoncardshop.catalog.internal.domain.CatalogImportErrorCode
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.CardCount
import com.zoonza.pokemoncardshop.catalog.internal.domain.expansion.ExpansionImage
import com.zoonza.pokemoncardshop.catalog.internal.domain.shared.Name
import com.zoonza.pokemoncardshop.catalog.test.fake.TEST_REGISTERED_AT
import com.zoonza.pokemoncardshop.catalog.test.fake.sourceExpansionFixture
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SourceExpansionTests {
    @Test
    fun `원본 확장팩을 확장팩 등록 정보로 변환한다`() {
        val info = sourceExpansionFixture().toExpansionRegisterInfo(
            seriesId = 1L,
            nameKo = "스칼렛&바이올렛",
            registeredAt = TEST_REGISTERED_AT,
        )

        info.seriesId shouldBe 1L
        info.sourceId shouldBe "sv01"
        info.name shouldBe Name(en = "Scarlet & Violet", ko = "스칼렛&바이올렛")
        info.count shouldBe CardCount(total = 258, official = 198)
        info.image shouldBe ExpansionImage(
            logoUrl = "https://image/sv01.png",
            symbolUrl = "https://image/sv01-symbol.png",
        )
        info.releaseDate shouldBe LocalDate.of(2023, 3, 31)
        info.registeredAt shouldBe TEST_REGISTERED_AT
    }

    @Test
    fun `로고가 없으면 확장팩 등록 정보로 변환하지 않는다`() {
        val exception = shouldThrow<DomainException> {
            sourceExpansionFixture(logoUrl = null).toExpansionRegisterInfo(
                seriesId = 1L,
                nameKo = "스칼렛&바이올렛",
                registeredAt = TEST_REGISTERED_AT,
            )
        }

        exception.errorCode shouldBe CatalogImportErrorCode.EXPANSION_LOGO_REQUIRED
    }

    @Test
    fun `한글 이름이 비어 있으면 확장팩 등록 정보로 변환하지 않는다`() {
        val exception = shouldThrow<DomainException> {
            sourceExpansionFixture().toExpansionRegisterInfo(
                seriesId = 1L,
                nameKo = " ",
                registeredAt = TEST_REGISTERED_AT,
            )
        }

        exception.errorCode shouldBe CatalogImportErrorCode.EXPANSION_KOREAN_NAME_REQUIRED
    }
}
