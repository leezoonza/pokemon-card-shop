package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.auth.internal.application.dto.RefreshResult

data class RefreshResponse(
    val accessToken: String,
    val role: String,
) {
    constructor(
        result: RefreshResult
    ) : this(
        accessToken = result.tokens.accessToken.value,
        role = result.role
    )
}
