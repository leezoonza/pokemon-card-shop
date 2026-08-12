package com.zoonza.pokemoncardshop.auth.internal.adapter.out.redis

import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.test.fake.verifiedExternalIdentityFixture
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Duration
import kotlin.io.encoding.Base64

class RedisIdentityTicketStoreAdapterTests {

    private val redisTemplate = mockk<StringRedisTemplate>()
    private val valueOperations = mockk<ValueOperations<String, String>>()
    private val objectMapper = jacksonObjectMapper()
    private val store = RedisIdentityTicketStoreAdapter(objectMapper, redisTemplate)

    @Test
    fun `연동 계정을 일회용 티켓으로 저장한다`() {
        val identity = verifiedExternalIdentityFixture()
        val ttl = Duration.ofMinutes(10)
        val key = slot<String>()
        val value = objectMapper.writeValueAsString(identity)
        every { redisTemplate.opsForValue() } returns valueOperations
        every { valueOperations.set(capture(key), value, ttl) } just Runs

        val ticket = store.issue(identity, ttl)

        key.captured shouldBe "auth:identity:ticket:$ticket"
        Base64.UrlSafe
            .withPadding(Base64.PaddingOption.ABSENT)
            .decode(ticket)
            .size shouldBe 16
        verify(exactly = 1) { valueOperations.set(key.captured, value, ttl) }
    }

    @Test
    fun `저장된 신원 티켓을 한 번만 소비한다`() {
        val identity = verifiedExternalIdentityFixture()
        val value = objectMapper.writeValueAsString(identity)
        every { redisTemplate.opsForValue() } returns valueOperations
        every {
            valueOperations.getAndDelete("auth:identity:ticket:identity-ticket")
        } returns value

        val result = store.consume("identity-ticket")

        result shouldBe identity
        verify(exactly = 1) {
            valueOperations.getAndDelete("auth:identity:ticket:identity-ticket")
        }
    }

    @Test
    fun `이전 식별자 필드로 저장된 신원 티켓도 소비한다`() {
        val value = """{"provider":"GOOGLE","identifier":"google-subject"}"""
        every { redisTemplate.opsForValue() } returns valueOperations
        every {
            valueOperations.getAndDelete("auth:identity:ticket:identity-ticket")
        } returns value

        val result = store.consume("identity-ticket")

        result shouldBe verifiedExternalIdentityFixture()
    }

    @Test
    fun `저장되지 않은 신원 티켓은 거절한다`() {
        every { redisTemplate.opsForValue() } returns valueOperations
        every {
            valueOperations.getAndDelete("auth:identity:ticket:invalid-ticket")
        } returns null

        val exception = shouldThrow<DomainException> {
            store.consume("invalid-ticket")
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_IDENTITY_TICKET
    }
}
