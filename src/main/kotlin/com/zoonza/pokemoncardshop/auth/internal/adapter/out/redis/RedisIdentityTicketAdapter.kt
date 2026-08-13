package com.zoonza.pokemoncardshop.auth.internal.adapter.out.redis

import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketPort
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.readValue
import java.security.SecureRandom
import java.time.Duration
import kotlin.io.encoding.Base64

@Component
class RedisIdentityTicketAdapter(
    private val objectMapper: ObjectMapper,
    private val redisTemplate: StringRedisTemplate
) : IdentityTicketPort {
    private val secureRandom = SecureRandom()

    override fun issue(
        identity: VerifiedExternalIdentity,
        ttl: Duration
    ): String {
        val ticket = generateTicket()
        val key = createKey(ticket)
        val value = serialize(identity)

        redisTemplate
            .opsForValue()
            .set(key, value, ttl)

        return ticket
    }

    override fun consume(identityTicket: String): VerifiedExternalIdentity {
        val key = createKey(identityTicket)

        val value = redisTemplate
            .opsForValue()
            .getAndDelete(key)
            ?: throw DomainException(AuthErrorCode.INVALID_IDENTITY_TICKET)

        return deserialize(value)
    }

    private fun generateTicket(): String {
        val bytes = ByteArray(TICKET_BYTE_LENGTH)

        secureRandom.nextBytes(bytes)

        return Base64.UrlSafe
            .withPadding(Base64.PaddingOption.ABSENT)
            .encode(bytes)

    }

    private fun createKey(ticket: String): String = "$KEY_PREFIX$ticket"

    private fun serialize(identity: VerifiedExternalIdentity): String =
        objectMapper.writeValueAsString(identity)

    private fun deserialize(value: String): VerifiedExternalIdentity =
        objectMapper.readValue(value)


    companion object {
        private const val TICKET_BYTE_LENGTH: Int = 16
        private const val KEY_PREFIX: String = "auth:identity:ticket:"
    }
}