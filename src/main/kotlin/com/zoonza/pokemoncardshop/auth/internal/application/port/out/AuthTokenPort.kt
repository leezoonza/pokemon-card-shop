package com.zoonza.pokemoncardshop.auth.internal.application.port.out

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAuthTokens

interface AuthTokenPort {
    fun issue(memberId: Long, role: String): IssuedAuthTokens
}
