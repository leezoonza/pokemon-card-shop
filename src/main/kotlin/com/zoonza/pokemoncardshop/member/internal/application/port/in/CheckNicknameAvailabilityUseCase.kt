package com.zoonza.pokemoncardshop.member.internal.application.port.`in`

import com.zoonza.pokemoncardshop.member.internal.domain.Nickname

interface CheckNicknameAvailabilityUseCase {
    fun check(nickname: Nickname): Boolean
}