package com.zoonza.pokemoncardshop.auth.internal.application.dto

data class AuthenticationResult(
    val tokens: IssuedAuthTokens,
    val role: String,
)
