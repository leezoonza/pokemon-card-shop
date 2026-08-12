package com.zoonza.pokemoncardshop.auth.internal.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class ExternalAccountTests {

    @ParameterizedTest
    @EnumSource(ExternalAccountProvider::class)
    fun `연동 계정을 가입 상태로 생성한다`(provider: ExternalAccountProvider) {
        val createdAt = Instant.parse("2026-08-02T03:00:00Z")

        val identity = ExternalAccount.register(
            provider = provider,
            subject = "google-subject",
            memberId = 42L,
            linkedAt = createdAt,
        )

        identity.id shouldBe 0L
        identity.provider shouldBe provider
        identity.subject shouldBe "google-subject"
        identity.memberId shouldBe 42L
        identity.linkedAt shouldBe createdAt
    }
}
