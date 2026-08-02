package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import com.zoonza.pokemoncardshop.auth.internal.adapter.out.support.SecureTokenGenerator
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class OpaqueRefreshTokenGeneratorTests {

    private val secureTokenGenerator = mockk<SecureTokenGenerator>()

    @Test
    fun `32바이트 난수로 리프레시 토큰을 생성한다`() {
        val ttl = Duration.ofDays(14)
        val generator = OpaqueRefreshTokenGenerator(
            OpaqueTokenProperties(ttl),
            secureTokenGenerator,
        )

        every { secureTokenGenerator.generate(32) } returns "refresh-token"

        val token = generator.generate(Instant.parse("2026-08-02T03:00:00Z"))

        token.value shouldBe "refresh-token"
        token.ttl shouldBe ttl

        verify(exactly = 1) { secureTokenGenerator.generate(32) }
    }
}
