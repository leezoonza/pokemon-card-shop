package com.zoonza.pokemoncardshop.member.internal.application.port.`in`

import com.zoonza.pokemoncardshop.member.api.MemberLoginResult

interface MemberAuthenticator {
    fun login(identityTicket: String): MemberLoginResult
}