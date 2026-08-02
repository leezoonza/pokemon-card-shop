package com.zoonza.pokemoncardshop.auth.internal.application.dto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SignupCommandTests {

    @Test
    fun `닉네임과 신원 티켓으로 가입 명령을 생성한다`() {
        val command = SignupCommand("피카츄", "identity-ticket")

        command.nickname shouldBe "피카츄"
        command.identityTicket shouldBe "identity-ticket"
    }
}
