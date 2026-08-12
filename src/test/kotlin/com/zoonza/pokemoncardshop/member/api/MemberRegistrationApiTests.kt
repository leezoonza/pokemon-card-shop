package com.zoonza.pokemoncardshop.member.api

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberFinder
import com.zoonza.pokemoncardshop.member.internal.application.service.MemberCommandService
import com.zoonza.pokemoncardshop.member.internal.domain.*
import com.zoonza.pokemoncardshop.member.test.fake.TEST_MEMBER_CREATED_AT
import com.zoonza.pokemoncardshop.member.test.fake.persistedMemberFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test

class MemberRegistrationApiTests {

    private val memberFinder = mockk<MemberFinder>()
    private val memberRepository = mockk<MemberRepository>()
    private val memberRegistrationApi: MemberRegistrationApi = MemberCommandService(
        memberFinder = memberFinder,
        memberRepository = memberRepository,
    )

    @Test
    fun `사용 가능한 닉네임으로 일반 회원을 가입시킨다`() {
        val memberSlot = slot<Member>()
        val command = MemberRegisterCommand(
            nickname = "피카츄",
            createdAt = TEST_MEMBER_CREATED_AT,
        )
        every { memberRepository.existsByNickname(Nickname("피카츄")) } returns false
        every { memberRepository.save(capture(memberSlot)) } returns persistedMemberFixture()

        val result = memberRegistrationApi.register(command)

        result shouldBe MemberRegisterResult(memberId = 42L, role = "MEMBER")
        with(memberSlot.captured) {
            nickname shouldBe Nickname("피카츄")
            role shouldBe MemberRole.MEMBER
            createdAt shouldBe TEST_MEMBER_CREATED_AT
            lastLoginAt shouldBe TEST_MEMBER_CREATED_AT
        }
        verify(exactly = 1) {
            memberRepository.existsByNickname(Nickname("피카츄"))
            memberRepository.save(memberSlot.captured)
        }
    }

    @Test
    fun `올바르지 않은 닉네임으로 가입할 수 없다`() {
        val exception = shouldThrow<DomainException> {
            memberRegistrationApi.register(
                MemberRegisterCommand("피", TEST_MEMBER_CREATED_AT),
            )
        }

        exception.errorCode shouldBe MemberErrorCode.INVALID_NICKNAME
        verify(exactly = 0) {
            memberRepository.existsByNickname(any())
            memberRepository.save(any())
        }
    }

    @Test
    fun `이미 사용 중인 닉네임으로 가입할 수 없다`() {
        val nickname = Nickname("피카츄")
        every { memberRepository.existsByNickname(nickname) } returns true

        val exception = shouldThrow<DomainException> {
            memberRegistrationApi.register(
                MemberRegisterCommand(nickname.value, TEST_MEMBER_CREATED_AT),
            )
        }

        exception.errorCode shouldBe MemberErrorCode.DUPLICATE_NICKNAME
        verify(exactly = 0) { memberRepository.save(any()) }
    }
}
