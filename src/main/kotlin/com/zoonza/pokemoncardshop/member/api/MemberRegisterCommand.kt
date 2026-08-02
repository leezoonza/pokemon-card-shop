package com.zoonza.pokemoncardshop.member.api

import java.time.Instant

data class MemberRegisterCommand(
    val nickname: String,
    val createdAt: Instant,
)
