package com.zoonza.pokemoncardshop.global.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationConverter : Converter<Jwt, AbstractAuthenticationToken> {

    private val authoritiesConverter = JwtGrantedAuthoritiesConverter().apply {
        setAuthoritiesClaimName(ROLE_CLAIM)
        setAuthorityPrefix(ROLE_PREFIX)
    }

    override fun convert(source: Jwt): AbstractAuthenticationToken {
        val memberId = source.subject
            ?.toLongOrNull()
            ?: throw InvalidBearerTokenException(INVALID_SUBJECT_MESSAGE)

        return JwtAuthenticationToken(
            source,
            memberId,
            authoritiesConverter.convert(source),
        )
    }

    companion object {
        private const val ROLE_CLAIM = "role"
        private const val ROLE_PREFIX = "ROLE_"
        private const val INVALID_SUBJECT_MESSAGE = "유효하지 않은 JWT subject 입니다."
    }
}
