package com.zoonza.pokemoncardshop.auth.internal.application.port.out

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthTokens

interface AuthTokenIssuer {
    fun issue(memberId: Long, role: String): AuthTokens
}