package com.zoonza.pokemoncardshop.common.response

import com.zoonza.pokemoncardshop.common.error.CommonErrorCode
import com.zoonza.pokemoncardshop.common.error.ValidationError
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test

class ErrorResponseTests {

    @Test
    fun `검증 오류 응답은 전달된 오류 목록을 복사한다`() {
        val errors = mutableListOf(ValidationError("name", "이름은 필수입니다."))
        val response = ErrorResponse.validation(CommonErrorCode.VALIDATION_FAILED, errors)

        errors.clear()

        response.errors shouldContainExactly listOf(
            ValidationError("name", "이름은 필수입니다."),
        )
    }
}
