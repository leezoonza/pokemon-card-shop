package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthTokens

interface LoginUseCase {
    fun login(identityTicket: String): AuthTokens
}