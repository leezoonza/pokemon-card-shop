package com.zoonza.pokemoncardshop.auth.internal.application.dto

data class RefreshResult(
    val tokens: IssuedAuthTokens,
    val role: String,
)
