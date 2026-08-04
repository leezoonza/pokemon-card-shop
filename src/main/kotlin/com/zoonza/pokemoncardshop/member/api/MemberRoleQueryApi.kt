package com.zoonza.pokemoncardshop.member.api

interface MemberRoleQueryApi {
    fun findByMemberId(memberId: Long): MemberRoleResult?
}
