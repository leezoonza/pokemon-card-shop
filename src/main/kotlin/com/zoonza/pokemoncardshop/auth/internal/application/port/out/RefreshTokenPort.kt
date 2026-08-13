package com.zoonza.pokemoncardshop.auth.internal.application.port.out

import java.time.Duration

interface RefreshTokenPort {
    fun save(
        memberId: Long,
        refreshToken: String,
        ttl: Duration
    )

    fun consume(refreshToken: String): Long

    fun delete(refreshToken: String)
}

