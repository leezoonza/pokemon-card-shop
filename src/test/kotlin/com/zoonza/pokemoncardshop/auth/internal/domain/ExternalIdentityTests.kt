package com.zoonza.pokemoncardshop.auth.internal.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class ExternalIdentityTests {

    @ParameterizedTest
    @EnumSource(IdentityProvider::class)
    fun `외부 신원을 가입 상태로 생성한다`(provider: IdentityProvider) {
        val createdAt = Instant.parse("2026-08-02T03:00:00Z")

        val identity = ExternalIdentity.register(
            provider = provider,
            subject = "google-subject",
            memberId = 42L,
            createdAt = createdAt,
        )

        identity.id shouldBe 0L
        identity.provider shouldBe provider
        identity.subject shouldBe "google-subject"
        identity.memberId shouldBe 42L
        identity.createdAt shouldBe createdAt
        identity.lastAuthenticatedAt shouldBe createdAt
    }
}
