package com.zoonza.pokemoncardshop.member.internal.domain

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.test.fake.persistedMemberFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
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
        member.status shouldBe MemberStatus.ACTIVE
        member.createdAt shouldBe createdAt
        member.lastLoginAt shouldBe createdAt
    }

    @Test
    fun `로그인 시각을 마지막 로그인 시각으로 기록한다`() {
        val createdAt = Instant.parse("2026-07-31T03:30:00Z")
        val loggedInAt = Instant.parse("2026-08-04T04:00:00Z")
        val member = Member.register(
            nickname = Nickname("피카츄"),
            role = MemberRole.MEMBER,
            createdAt = createdAt,
        )

        member.login(loggedInAt)

        member.lastLoginAt shouldBe loggedInAt
    }

    @Test
    fun `닉네임을 변경한다`() {
        val member = Member.register(
            nickname = Nickname("피카츄"),
            role = MemberRole.MEMBER,
            createdAt = Instant.parse("2026-07-31T03:30:00Z"),
        )
        val newNickname = Nickname("라이츄")

        member.updateNickname(newNickname)

        member.nickname shouldBe newNickname
    }

    @Test
    fun `비활성화된 회원은 로그인할 수 없다`() {
        val member = persistedMemberFixture(status = MemberStatus.DEACTIVATED)

        val exception = shouldThrow<DomainException> {
            member.login(Instant.parse("2026-08-04T04:00:00Z"))
        }

        exception.errorCode shouldBe MemberErrorCode.DEACTIVATED_MEMBER
    }

    @Test
    fun `비활성화된 회원은 닉네임을 변경할 수 없다`() {
        val member = persistedMemberFixture(status = MemberStatus.DEACTIVATED)

        val exception = shouldThrow<DomainException> {
            member.updateNickname(Nickname("라이츄"))
        }

        exception.errorCode shouldBe MemberErrorCode.DEACTIVATED_MEMBER
        member.nickname shouldBe Nickname("피카츄")
    }
}
