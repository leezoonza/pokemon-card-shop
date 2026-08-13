package com.zoonza.pokemoncardshop.auth.internal.adapter.out.redis

import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

class RedisRefreshTokenAdapterTests {

    private val redisTemplate = mockk<StringRedisTemplate>()
    private val valueOperations = mockk<ValueOperations<String, String>>()
    private val store = RedisRefreshTokenAdapter(redisTemplate)

    @Test
    fun `회원의 리프레시 토큰을 만료 시간과 함께 저장한다`() {
        val ttl = Duration.ofDays(14)
        every { redisTemplate.opsForValue() } returns valueOperations
        every {
            valueOperations.set("auth:refresh:token:refresh-token", "42", ttl)
        } just Runs

        store.save(42L, "refresh-token", ttl)

        verify(exactly = 1) {
            valueOperations.set("auth:refresh:token:refresh-token", "42", ttl)
        }
    }

    @Test
    fun `저장된 리프레시 토큰을 한 번만 소비한다`() {
        every { redisTemplate.opsForValue() } returns valueOperations
        every {
            valueOperations.getAndDelete("auth:refresh:token:refresh-token")
        } returns "42"

        val memberId = store.consume("refresh-token")

        memberId shouldBe 42L
        verify(exactly = 1) {
            valueOperations.getAndDelete("auth:refresh:token:refresh-token")
        }
    }

    @Test
    fun `저장되지 않은 리프레시 토큰은 거절한다`() {
        every { redisTemplate.opsForValue() } returns valueOperations
        every {
            valueOperations.getAndDelete("auth:refresh:token:invalid-token")
        } returns null

        val exception = shouldThrow<DomainException> {
            store.consume("invalid-token")
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
    }

    @Test
    fun `리프레시 토큰을 삭제한다`() {
        every {
            redisTemplate.delete("auth:refresh:token:refresh-token")
        } returns true

        store.delete("refresh-token")

        verify(exactly = 1) {
            redisTemplate.delete("auth:refresh:token:refresh-token")
        }
    }
}
