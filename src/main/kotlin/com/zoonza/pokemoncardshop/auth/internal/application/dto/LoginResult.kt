package com.zoonza.pokemoncardshop.auth.internal.application.dto

data class LoginResult(
    val tokens: IssuedAuthTokens,
    val role: String,
)
