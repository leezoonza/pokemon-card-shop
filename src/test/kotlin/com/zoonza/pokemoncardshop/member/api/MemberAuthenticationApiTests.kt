package com.zoonza.pokemoncardshop.member.api

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberFinder
import com.zoonza.pokemoncardshop.member.internal.application.service.MemberAuthenticationService
import com.zoonza.pokemoncardshop.member.internal.domain.MemberErrorCode
import com.zoonza.pokemoncardshop.member.test.fake.persistedMemberFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class MemberAuthenticationApiTests {

    private val memberFinder = mockk<MemberFinder>()
    private val memberAuthenticationApi: MemberAuthenticationApi =
        MemberAuthenticationService(memberFinder)

    @Test
    fun `회원의 마지막 로그인 시각을 기록하고 역할을 반환한다`() {
        val member = persistedMemberFixture()
        val loggedInAt = Instant.parse("2026-08-04T04:00:00Z")
        every { memberFinder.findById(42L) } returns member

        val result = memberAuthenticationApi.login(MemberLoginCommand(42L, loggedInAt))

        result shouldBe MemberLoginResult("MEMBER")
        member.lastLoginAt shouldBe loggedInAt
        verify(exactly = 1) { memberFinder.findById(42L) }
    }

    @Test
    fun `존재하지 않는 회원은 로그인할 수 없다`() {
        every { memberFinder.findById(42L) } throws
                DomainException(MemberErrorCode.MEMBER_NOT_FOUND)

        val exception = shouldThrow<DomainException> {
            memberAuthenticationApi.login(
                MemberLoginCommand(42L, Instant.parse("2026-08-04T04:00:00Z")),
            )
        }

        exception.errorCode shouldBe MemberErrorCode.MEMBER_NOT_FOUND
    }

    @Test
    fun `회원의 역할을 조회한다`() {
        every { memberFinder.findById(42L) } returns persistedMemberFixture()

        memberAuthenticationApi.getMemberRole(42L) shouldBe MemberRoleResult("MEMBER")

        verify(exactly = 1) { memberFinder.findById(42L) }
    }

    @Test
    fun `존재하지 않는 회원의 역할은 조회할 수 없다`() {
        every { memberFinder.findById(42L) } throws
                DomainException(MemberErrorCode.MEMBER_NOT_FOUND)

        val exception = shouldThrow<DomainException> {
            memberAuthenticationApi.getMemberRole(42L)
        }

        exception.errorCode shouldBe MemberErrorCode.MEMBER_NOT_FOUND
    }
}
