package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import jakarta.validation.Validation
import org.junit.jupiter.api.Test

class SignupRequestTests {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `두 글자 닉네임은 허용한다`() {
        val request = SignupRequest(nickname = "피카")

        validator.validate(request).shouldBeEmpty()
    }

    @Test
    fun `열네 글자 닉네임은 허용한다`() {
        val request = SignupRequest(nickname = "가나다라마바사아자차카타파하")

        validator.validate(request).shouldBeEmpty()
    }

    @Test
    fun `두 글자보다 짧은 닉네임은 거절한다`() {
        val request = SignupRequest(nickname = "피")

        validator.validate(request)
            .map { it.message }
            .shouldContainExactly(INVALID_NICKNAME_MESSAGE)
    }

    @Test
    fun `열네 글자보다 긴 닉네임은 거절한다`() {
        val request = SignupRequest(nickname = "가나다라마바사아자차카타파하나")

        validator.validate(request)
            .map { it.message }
            .shouldContainExactly(INVALID_NICKNAME_MESSAGE)
    }

    @Test
    fun `특수문자가 포함된 닉네임은 거절한다`() {
        val request = SignupRequest(nickname = "피카츄!")

        validator.validate(request)
            .map { it.message }
            .shouldContainExactly(INVALID_NICKNAME_MESSAGE)
    }

    @Test
    fun `빈 닉네임은 필수 입력 오류만 반환한다`() {
        val request = SignupRequest(nickname = "")

        validator.validate(request)
            .map { it.message }
            .shouldContainExactly("닉네임을 입력해 주세요.")
    }

    companion object {
        private const val INVALID_NICKNAME_MESSAGE =
            "닉네임은 2자 이상 14자 이하의 한글, 영문, 숫자로 입력해 주세요."
    }
}
