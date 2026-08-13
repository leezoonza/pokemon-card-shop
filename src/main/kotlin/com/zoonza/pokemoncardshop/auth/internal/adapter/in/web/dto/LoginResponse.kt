package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.auth.internal.application.dto.LoginResult

data class LoginResponse(
    val accessToken: String,
    val role: String,
) {
    constructor(
        result: LoginResult
    ) : this(
        accessToken = result.tokens.accessToken.value,
        role = result.role,
    )
}
