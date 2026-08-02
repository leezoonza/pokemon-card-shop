package com.zoonza.pokemoncardshop.global.config

import io.kotest.matchers.collections.shouldContain
import io.mockk.mockk
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler

class SecurityConfigTests {

    private val config = SecurityConfig(
        allowedOrigin = "http://localhost:3000",
        oidcUserService = mockk<OidcUserService>(),
        authenticationSuccessHandler = mockk<AuthenticationSuccessHandler>(),
        authenticationFailureHandler = mockk<AuthenticationFailureHandler>(),
    )

    @ParameterizedTest
    @ValueSource(strings = ["MEMBER", "ADMIN"])
    fun `역할 클레임을 스프링 시큐리티 권한으로 변환한다`(role: String) {
        val jwt = Jwt.withTokenValue("access-token")
            .header("alg", "HS256")
            .subject("1")
            .claim("role", role)
            .build()

        val authentication = config.jwtAuthenticationConverter().convert(jwt)

        authentication.authorities
            .map { it.authority }
            .shouldContain("ROLE_$role")
    }
}
