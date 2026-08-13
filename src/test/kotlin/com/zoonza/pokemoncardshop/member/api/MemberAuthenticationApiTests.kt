package com.zoonza.pokemoncardshop.member.api

import com.zoonza.pokemoncardshop.member.internal.application.service.MemberAuthenticationService
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRepository
import com.zoonza.pokemoncardshop.member.test.fake.persistedMemberFixture
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class MemberAuthenticationApiTests {

    private val memberRepository = mockk<MemberRepository>()
    private val memberAuthenticationApi: MemberAuthenticationApi =
        MemberAuthenticationService(memberRepository)

    @Test
    fun `회원의 마지막 로그인 시각을 기록하고 역할을 반환한다`() {
        val member = persistedMemberFixture()
        val loggedInAt = Instant.parse("2026-08-04T04:00:00Z")
        every { memberRepository.findByIdOrNull(42L) } returns member

        val result = memberAuthenticationApi.login(MemberLoginCommand(42L, loggedInAt))

        result shouldBe MemberLoginResult("MEMBER")
        member.lastLoginAt shouldBe loggedInAt
        verify(exactly = 1) { memberRepository.findByIdOrNull(42L) }
    }

    @Test
    fun `존재하지 않는 회원은 로그인할 수 없다`() {
        every { memberRepository.findByIdOrNull(42L) } returns null

        memberAuthenticationApi.login(
            MemberLoginCommand(42L, Instant.parse("2026-08-04T04:00:00Z")),
        ) shouldBe null
    }

    @Test
    fun `회원의 역할을 조회한다`() {
        every { memberRepository.findByIdOrNull(42L) } returns persistedMemberFixture()

        memberAuthenticationApi.getMemberRole(42L) shouldBe MemberRoleResult("MEMBER")

        verify(exactly = 1) { memberRepository.findByIdOrNull(42L) }
    }

    @Test
    fun `존재하지 않는 회원의 역할은 조회할 수 없다`() {
        every { memberRepository.findByIdOrNull(42L) } returns null

        memberAuthenticationApi.getMemberRole(42L) shouldBe null
    }
}
