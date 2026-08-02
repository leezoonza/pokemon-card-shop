package com.zoonza.pokemoncardshop.auth.internal.domain

import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class IdentityProviderTests {

    @ParameterizedTest
    @EnumSource(IdentityProvider::class)
    fun `외부 인증 제공자 값을 변환한다`(provider: IdentityProvider) {
        IdentityProvider.from(provider.value) shouldBe provider
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "GOOGLE", "naver"])
    fun `지원하지 않는 외부 인증 제공자는 거절한다`(value: String) {
        val exception = shouldThrow<DomainException> {
            IdentityProvider.from(value)
        }

        exception.errorCode shouldBe AuthErrorCode.UNSUPPORTED_IDENTITY_PROVIDER
    }
}
