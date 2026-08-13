package com.zoonza.pokemoncardshop.auth.internal.application.dto

data class SignupResult(
    val tokens: IssuedAuthTokens,
    val role: String,
)
