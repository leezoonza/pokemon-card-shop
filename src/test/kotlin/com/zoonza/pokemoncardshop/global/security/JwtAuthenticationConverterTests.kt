package com.zoonza.pokemoncardshop.global.security

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException

class JwtAuthenticationConverterTests {

    private val converter = JwtAuthenticationConverter()

    @ParameterizedTest
    @ValueSource(strings = ["MEMBER", "ADMIN"])
    fun `회원 식별자와 역할을 인증 정보로 변환한다`(role: String) {
        val jwt = jwt(subject = "42", role = role)

        val authentication = converter.convert(jwt)

        authentication.principal shouldBe 42L
        authentication.authorities
            .map { it.authority }
            .shouldContain("ROLE_$role")
    }

    @Test
    fun `숫자가 아닌 회원 식별자는 유효하지 않은 토큰으로 거절한다`() {
        val jwt = jwt(subject = "member-42", role = "MEMBER")

        shouldThrow<InvalidBearerTokenException> {
            converter.convert(jwt)
        }
    }

    private fun jwt(
        subject: String,
        role: String,
    ): Jwt = Jwt.withTokenValue("access-token")
        .header("alg", "HS256")
        .subject(subject)
        .claim("role", role)
        .build()
}
