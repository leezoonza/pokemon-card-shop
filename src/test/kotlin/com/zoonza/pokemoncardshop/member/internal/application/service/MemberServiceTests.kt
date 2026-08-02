package com.zoonza.pokemoncardshop.member.internal.application.service

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.MemberRegisterCommand
import com.zoonza.pokemoncardshop.member.internal.domain.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.Instant

class MemberServiceTests {

    private val memberRepository = mockk<MemberRepository>()
    private val memberService = MemberService(memberRepository)

    @Test
    fun `닉네임이 존재하지 않으면 사용할 수 있다`() {
        val nickname = Nickname("피카츄")

        every { memberRepository.existsByNickname(nickname) } returns false

        val available = memberService.check(nickname)

        available shouldBe true

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
    }

    @Test
    fun `닉네임이 존재하면 사용할 수 없다`() {
        val nickname = Nickname("피카츄")

        every { memberRepository.existsByNickname(nickname) } returns true

        val available = memberService.check(nickname)

        available shouldBe false

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
    }

    @Test
    fun `사용 가능한 닉네임으로 일반 회원을 가입시킨다`() {
        val nickname = Nickname("피카츄")
        val createdAt = Instant.parse("2026-07-31T03:30:00Z")
        val memberSlot = slot<Member>()
        val savedMember = persistedMember(
            id = 42L,
            nickname = nickname,
            createdAt = createdAt,
        )
        every { memberRepository.existsByNickname(nickname) } returns false
        every { memberRepository.save(capture(memberSlot)) } returns savedMember

        val result = memberService.register(
            MemberRegisterCommand(
                nickname = nickname.value,
                createdAt = createdAt,
            ),
        )

        result.memberId shouldBe 42L
        result.role shouldBe "MEMBER"

        memberSlot.captured.nickname shouldBe nickname
        memberSlot.captured.role shouldBe MemberRole.MEMBER
        memberSlot.captured.createdAt shouldBe createdAt
        memberSlot.captured.lastLoginAt shouldBe createdAt

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
        verify(exactly = 1) { memberRepository.save(memberSlot.captured) }
    }

    @Test
    fun `올바르지 않은 닉네임으로 가입할 수 없다`() {
        val exception = shouldThrow<DomainException> {
            memberService.register(
                MemberRegisterCommand(
                    nickname = "피",
                    createdAt = Instant.parse("2026-07-31T03:30:00Z"),
                ),
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
            memberService.register(
                MemberRegisterCommand(
                    nickname = nickname.value,
                    createdAt = Instant.parse("2026-07-31T03:30:00Z"),
                ),
            )
        }

        exception.errorCode shouldBe MemberErrorCode.DUPLICATE_NICKNAME

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    private fun persistedMember(
        id: Long,
        nickname: Nickname,
        createdAt: Instant,
    ): Member {
        val constructor = Member::class.java.getDeclaredConstructor(
            Long::class.javaPrimitiveType,
            Nickname::class.java,
            MemberRole::class.java,
            Instant::class.java,
            Instant::class.java,
        )
        constructor.isAccessible = true

        return constructor.newInstance(
            id,
            nickname,
            MemberRole.MEMBER,
            createdAt,
            createdAt,
        )
    }
}
