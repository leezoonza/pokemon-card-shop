package com.zoonza.pokemoncardshop.member.internal.application.port.`in`

import com.zoonza.pokemoncardshop.member.api.MemberLoginResult

interface MemberAuthenticationUseCase {
    fun login(identityTicket: String): MemberLoginResult
}