package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAccessToken
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedRefreshToken
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class AuthTokenIssuerAdapterTests {

    @Test
    fun `같은 발급 시각으로 액세스 토큰과 리프레시 토큰을 발급한다`() {
        val issuedAt = Instant.parse("2026-08-02T03:00:00Z")
        val accessTokenGenerator = mockk<JwtAccessTokenGenerator>()
        val refreshTokenGenerator = mockk<OpaqueRefreshTokenGenerator>()
        val accessToken = IssuedAccessToken("access-token")
        val refreshToken = IssuedRefreshToken("refresh-token", Duration.ofDays(14))

        every {
            accessTokenGenerator.generate(42L, "MEMBER", issuedAt)
        } returns accessToken

        every { refreshTokenGenerator.generate(issuedAt) } returns refreshToken

        val issuer = AuthTokenIssuerAdapter(
            Clock.fixed(issuedAt, ZoneOffset.UTC),
            accessTokenGenerator,
            refreshTokenGenerator,
        )

        val tokens = issuer.issue(42L, "MEMBER")

        tokens.accessToken shouldBe accessToken
        tokens.refreshToken shouldBe refreshToken

        verify(exactly = 1) {
            accessTokenGenerator.generate(42L, "MEMBER", issuedAt)
            refreshTokenGenerator.generate(issuedAt)
        }
    }
}
