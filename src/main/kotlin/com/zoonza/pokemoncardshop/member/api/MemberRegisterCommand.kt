package com.zoonza.pokemoncardshop.member.api

import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import java.time.LocalDateTime

data class MemberRegisterCommand(
    val nickname: Nickname,
    val createdAt: LocalDateTime,
)