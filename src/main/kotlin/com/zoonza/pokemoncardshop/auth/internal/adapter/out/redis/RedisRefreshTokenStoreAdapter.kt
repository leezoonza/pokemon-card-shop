package com.zoonza.pokemoncardshop.auth.internal.adapter.out.redis

import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.common.error.DomainException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisRefreshTokenStoreAdapter(
    private val redisTemplate: StringRedisTemplate
) : RefreshTokenStore {

    override fun save(
        memberId: Long,
        refreshToken: String,
        ttl: Duration
    ) {
        redisTemplate
            .opsForValue()
            .set(
                createKey(refreshToken),
                memberId.toString(),
                ttl
            )
    }

    override fun consume(refreshToken: String): Long {
        val memberId = redisTemplate
            .opsForValue()
            .getAndDelete(createKey(refreshToken))
            ?: throw DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        return memberId.toLong()
    }

    override fun delete(refreshToken: String) {
        redisTemplate.delete(createKey(refreshToken))
    }

    private fun createKey(refreshToken: String): String =
        "$KEY_PREFIX$refreshToken"

    companion object {
        private const val KEY_PREFIX: String = "auth:refresh:token:"
    }
}