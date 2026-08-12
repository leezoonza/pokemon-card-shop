package com.zoonza.pokemoncardshop.member.test.fake

import com.zoonza.pokemoncardshop.member.internal.domain.Member
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRole
import com.zoonza.pokemoncardshop.member.internal.domain.MemberStatus
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import java.time.Instant

val TEST_MEMBER_CREATED_AT: Instant = Instant.parse("2026-08-01T03:00:00Z")

fun memberFixture(
    nickname: Nickname = Nickname("피카츄"),
    role: MemberRole = MemberRole.MEMBER,
    createdAt: Instant = TEST_MEMBER_CREATED_AT,
): Member = Member.register(
    nickname = nickname,
    role = role,
    createdAt = createdAt,
)

fun persistedMemberFixture(
    id: Long = 42L,
    nickname: Nickname = Nickname("피카츄"),
    role: MemberRole = MemberRole.MEMBER,
    status: MemberStatus = MemberStatus.ACTIVE,
    createdAt: Instant = TEST_MEMBER_CREATED_AT,
    lastLoginAt: Instant = createdAt,
): Member {
    val constructor = Member::class.java.getDeclaredConstructor(
        Long::class.javaPrimitiveType,
        Nickname::class.java,
        MemberRole::class.java,
        MemberStatus::class.java,
        Instant::class.java,
        Instant::class.java,
    )
    constructor.isAccessible = true

    return constructor.newInstance(
        id,
        nickname,
        role,
        status,
        createdAt,
        lastLoginAt,
    )
}
