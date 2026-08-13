package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupResult

data class SignupResponse(
    val accessToken: String,
    val role: String,
) {
    constructor(
        result: SignupResult
    ) : this(
        accessToken = result.tokens.accessToken.value,
        role = result.role
    )
}
