package com.zoonza.pokemoncardshop.auth.internal.application.dto


data class AuthTokens(
    val accessToken: IssuedAccessToken,
    val refreshToken: IssuedRefreshToken,
)
