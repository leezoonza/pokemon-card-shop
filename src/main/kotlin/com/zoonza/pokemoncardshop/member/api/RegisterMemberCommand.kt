package com.zoonza.pokemoncardshop.member.api

import java.time.Instant

data class RegisterMemberCommand(
    val nickname: String,
    val createdAt: Instant,
)
