package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenIssuer
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

@Component
class AuthTokenIssuerAdapter(
    private val clock: Clock,
    private val accessTokenGenerator: JwtAccessTokenGenerator,
    private val refreshTokenGenerator: OpaqueRefreshTokenGenerator
) : AuthTokenIssuer {

    override fun issue(memberId: Long, role: String): AuthTokens {
        val issuedAt = Instant.now(clock)

        val accessToken = accessTokenGenerator.generate(memberId, role, issuedAt)
        val refreshToken = refreshTokenGenerator.generate(issuedAt)

        return AuthTokens(accessToken, refreshToken)
    }
}