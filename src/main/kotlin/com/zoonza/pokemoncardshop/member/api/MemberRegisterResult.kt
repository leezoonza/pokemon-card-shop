package com.zoonza.pokemoncardshop.member.api

import com.zoonza.pokemoncardshop.member.internal.domain.MemberRole

data class MemberRegisterResult(
    val memberId: Long,
    val role: MemberRole
)
