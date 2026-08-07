package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto

data class AuthenticationResponse(
    val accessToken: String,
    val role: String,
)
