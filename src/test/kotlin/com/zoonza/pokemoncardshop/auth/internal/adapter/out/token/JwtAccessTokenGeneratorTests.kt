package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.JwtTimestampValidator
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class JwtAccessTokenGeneratorTests {

    @Test
    fun `회원 식별자와 역할을 담은 액세스 토큰을 생성한다`() {
        val issuedAt = Instant.parse("2026-08-02T03:00:00Z")
        val properties = JwtTokenProperties(
            secret = "01234567890123456789012345678901",
            ttl = Duration.ofMinutes(30),
        )
        val config = JwtTokenConfig()
        val generator = JwtAccessTokenGenerator(
            config.jwtEncoder(properties),
            properties,
        )

        val issuedToken = generator.generate(42L, "MEMBER", issuedAt)
        val decoder = config.jwtDecoder(properties) as NimbusJwtDecoder
        val timestampValidator = JwtTimestampValidator().apply {
            setClock(Clock.fixed(issuedAt.plusSeconds(1), ZoneOffset.UTC))
        }

        decoder.setJwtValidator(timestampValidator)

        val decodedToken = decoder.decode(issuedToken.value)

        decodedToken.subject shouldBe "42"
        decodedToken.getClaimAsString("role") shouldBe "MEMBER"
        decodedToken.issuedAt shouldBe issuedAt
        decodedToken.expiresAt shouldBe issuedAt.plus(properties.ttl)
        decodedToken.id.shouldNotBeBlank()
    }
}
