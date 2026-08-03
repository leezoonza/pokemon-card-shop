package com.zoonza.pokemoncardshop.member.api

import java.time.Instant

data class MemberLoginCommand(
    val memberId: Long,
    val loggedInAt: Instant
)
