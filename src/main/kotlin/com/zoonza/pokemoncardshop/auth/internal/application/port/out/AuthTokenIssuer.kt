package com.zoonza.pokemoncardshop.auth.internal.application.port.out

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAuthTokens

interface AuthTokenIssuer {
    fun issue(memberId: Long, role: String): IssuedAuthTokens
}
