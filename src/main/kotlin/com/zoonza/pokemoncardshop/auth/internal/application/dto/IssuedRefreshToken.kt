package com.zoonza.pokemoncardshop.auth.internal.application.dto

import java.time.Duration

data class IssuedRefreshToken(
    val value: String,
    val ttl: Duration
)
