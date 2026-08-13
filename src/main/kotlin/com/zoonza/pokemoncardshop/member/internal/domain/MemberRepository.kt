package com.zoonza.pokemoncardshop.member.internal.domain

interface MemberRepository {
    fun existsByNickname(nickname: Nickname): Boolean

    fun save(member: Member): Member

    fun findByIdOrThrow(id: Long): Member

    fun findByIdOrNull(id: Long): Member?
}