package com.zoonza.pokemoncardshop.common.error

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DomainExceptionTests {

    @Test
    fun `도메인 예외는 오류 코드와 원인을 보존한다`() {
        val cause = IllegalStateException("cause")

        val exception = DomainException(CommonErrorCode.INTERNAL_SERVER_ERROR, cause)

        exception.errorCode shouldBe CommonErrorCode.INTERNAL_SERVER_ERROR
        exception.message shouldBe CommonErrorCode.INTERNAL_SERVER_ERROR.message
        exception.cause shouldBe cause
    }
}
