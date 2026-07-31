package com.zoonza.pokemoncardshop.member.internal.domain

import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class NicknameTests {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "피카",
            "가나다라마바사아자차카타파하",
            "Pikachu123",
            "피카츄25",
        ],
    )
    fun `허용된 문자와 길이의 닉네임을 생성한다`(value: String) {
        val nickname = Nickname(value)

        nickname.value shouldBe value
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " ", "\t"])
    fun `비어 있는 닉네임은 거절한다`(value: String) {
        assertInvalidNickname(value)
    }

    @Test
    fun `두 글자보다 짧은 닉네임은 거절한다`() {
        assertInvalidNickname("피")
    }

    @Test
    fun `열네 글자보다 긴 닉네임은 거절한다`() {
        assertInvalidNickname("가나다라마바사아자차카타파하나")
    }

    @ParameterizedTest
    @ValueSource(strings = ["피카 츄", "피카츄!", "Pika_chu", "Poké"])
    fun `한글 영문 숫자 이외의 문자가 포함된 닉네임은 거절한다`(value: String) {
        assertInvalidNickname(value)
    }

    private fun assertInvalidNickname(value: String) {
        val exception = shouldThrow<DomainException> {
            Nickname(value)
        }

        exception.errorCode shouldBe MemberErrorCode.INVALID_NICKNAME
    }
}
