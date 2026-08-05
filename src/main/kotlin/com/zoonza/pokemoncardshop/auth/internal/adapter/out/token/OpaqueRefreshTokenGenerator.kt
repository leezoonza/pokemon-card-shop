package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import com.zoonza.pokemoncardshop.auth.internal.adapter.out.support.SecureTokenGenerator
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedRefreshToken
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.time.Instant

@Component
@EnableConfigurationProperties(OpaqueTokenProperties::class)
class OpaqueRefreshTokenGenerator(
    private val properties: OpaqueTokenProperties,
    private val secureTokenGenerator: SecureTokenGenerator
) {
    fun generate(issuedAt: Instant): IssuedRefreshToken {
        val tokenValue = secureTokenGenerator.generate(TOKEN_BYTE_LENGTH)

        return IssuedRefreshToken(tokenValue, properties.ttl)
    }

    companion object {
        private const val TOKEN_BYTE_LENGTH = 32
    }
}