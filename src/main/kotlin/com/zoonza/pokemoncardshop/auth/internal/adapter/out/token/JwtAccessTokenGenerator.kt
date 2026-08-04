package com.zoonza.pokemoncardshop.auth.internal.adapter.out.token

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAccessToken
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*


@Component
class JwtAccessTokenGenerator(
    private val encoder: JwtEncoder,
    private val properties: JwtTokenProperties
) {
    fun generate(
        memberId: Long,
        role: String,
        issuedAt: Instant,
    ): IssuedAccessToken {

        val claims = JwtClaimsSet.builder()
            .subject(memberId.toString())
            .claim(ROLE_CLAIM, role)
            .id(UUID.randomUUID().toString())
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plus(properties.ttl))
            .build()

        val header = JwsHeader
            .with(MacAlgorithm.HS256)
            .build()

        val tokenValue = encoder
            .encode(JwtEncoderParameters.from(header, claims))
            .tokenValue

        return IssuedAccessToken(tokenValue)
    }

    companion object {
        private const val ROLE_CLAIM: String = "role"
    }
}