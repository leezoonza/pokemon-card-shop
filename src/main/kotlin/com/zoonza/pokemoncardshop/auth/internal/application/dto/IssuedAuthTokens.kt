package com.zoonza.pokemoncardshop.auth.internal.application.dto

data class IssuedAuthTokens(
    val accessToken: IssuedAccessToken,
    val refreshToken: IssuedRefreshToken,
)
