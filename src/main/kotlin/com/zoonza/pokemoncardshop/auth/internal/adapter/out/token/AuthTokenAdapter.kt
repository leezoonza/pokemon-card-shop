package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenPort
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

@Component
class AuthTokenAdapter(
    private val clock: Clock,
    private val accessTokenGenerator: JwtAccessTokenGenerator,
    private val refreshTokenGenerator: OpaqueRefreshTokenGenerator
) : AuthTokenPort {

    override fun issue(memberId: Long, role: String): IssuedAuthTokens {
        val issuedAt = Instant.now(clock)

        val accessToken = accessTokenGenerator.generate(memberId, role, issuedAt)
        val refreshToken = refreshTokenGenerator.generate(issuedAt)

        return IssuedAuthTokens(accessToken, refreshToken)
    }
}
