package com.zoonza.pokemoncardshop.auth.internal.application.dto

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant

class SignupCommandTests {

    @Test
    fun `닉네임과 신원 티켓과 가입 시각으로 가입 명령을 생성한다`() {
        val createdAt = Instant.parse("2026-08-13T03:00:00Z")

        val command = SignupCommand("피카츄", "identity-ticket", createdAt)

        command.nickname shouldBe "피카츄"
        command.identityTicket shouldBe "identity-ticket"
        command.createdAt shouldBe createdAt
    }
}
