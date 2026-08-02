package com.zoonza.pokemoncardshop.member.internal.domain

import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class MemberTests {

    @ParameterizedTest
    @EnumSource(MemberRole::class)
    fun `회원을 가입 상태로 생성한다`(role: MemberRole) {
        val nickname = Nickname("피카츄")
        val createdAt = Instant.parse("2026-07-31T03:30:00Z")

        val member = Member.register(nickname, role, createdAt)

        member.id shouldBe 0L
        member.nickname shouldBe nickname
        member.role shouldBe role
        member.createdAt shouldBe createdAt
        member.lastLoginAt shouldBe createdAt
    }
}
