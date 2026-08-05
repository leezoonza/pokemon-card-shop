package com.zoonza.pokemoncardshop.member.internal.application.service

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.MemberLoginCommand
import com.zoonza.pokemoncardshop.member.api.RegisterMemberCommand
import com.zoonza.pokemoncardshop.member.internal.application.dto.ChangeNicknameCommand
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

        val available = memberService.isAvailable(nickname)

        available shouldBe true

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
    }

    @Test
    fun `닉네임이 존재하면 사용할 수 없다`() {
        val nickname = Nickname("피카츄")

        every { memberRepository.existsByNickname(nickname) } returns true

        val available = memberService.isAvailable(nickname)

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
            RegisterMemberCommand(
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
                RegisterMemberCommand(
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
                RegisterMemberCommand(
                    nickname = nickname.value,
                    createdAt = Instant.parse("2026-07-31T03:30:00Z"),
                ),
            )
        }

        exception.errorCode shouldBe MemberErrorCode.DUPLICATE_NICKNAME

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
        verify(exactly = 0) { memberRepository.save(any()) }
    }

    @Test
    fun `회원의 마지막 로그인 시각을 기록한다`() {
        val createdAt = Instant.parse("2026-07-31T03:30:00Z")
        val loggedInAt = Instant.parse("2026-08-04T04:00:00Z")
        val member = persistedMember(
            id = 42L,
            nickname = Nickname("피카츄"),
            createdAt = createdAt,
        )
        every { memberRepository.findById(42L) } returns member

        val result = memberService.recordLogin(
            MemberLoginCommand(
                memberId = 42L,
                loggedInAt = loggedInAt,
            ),
        )

        result?.role shouldBe MemberRole.MEMBER.value
        member.lastLoginAt shouldBe loggedInAt
        verify(exactly = 1) { memberRepository.findById(42L) }
    }

    @Test
    fun `존재하지 않는 회원의 로그인을 기록할 수 없다`() {
        every { memberRepository.findById(42L) } returns null

        val result = memberService.recordLogin(
            MemberLoginCommand(
                memberId = 42L,
                loggedInAt = Instant.parse("2026-08-04T04:00:00Z"),
            ),
        )

        result shouldBe null
        verify(exactly = 1) { memberRepository.findById(42L) }
    }

    @Test
    fun `회원의 인증 정보를 조회한다`() {
        val createdAt = Instant.parse("2026-07-31T03:30:00Z")
        val member = persistedMember(
            id = 42L,
            nickname = Nickname("피카츄"),
            createdAt = createdAt,
        )
        every { memberRepository.findById(42L) } returns member

        val result = memberService.findByMemberId(42L)

        result?.role shouldBe MemberRole.MEMBER.value
        verify(exactly = 1) { memberRepository.findById(42L) }
    }

    @Test
    fun `존재하지 않는 회원의 인증 정보는 조회되지 않는다`() {
        every { memberRepository.findById(42L) } returns null

        val result = memberService.findByMemberId(42L)

        result shouldBe null
        verify(exactly = 1) { memberRepository.findById(42L) }
    }

    @Test
    fun `사용 가능한 닉네임으로 변경한다`() {
        val member = persistedMember(
            id = 42L,
            nickname = Nickname("피카츄"),
            createdAt = Instant.parse("2026-07-31T03:30:00Z"),
        )
        val command = ChangeNicknameCommand(memberId = 42L, nickname = "라이츄")
        every { memberRepository.findById(command.memberId) } returns member
        every { memberRepository.existsByNickname(command.nickname) } returns false

        memberService.change(command)

        member.nickname shouldBe command.nickname
        verify(exactly = 1) { memberRepository.findById(command.memberId) }
        verify(exactly = 1) { memberRepository.existsByNickname(command.nickname) }
    }

    @Test
    fun `현재 닉네임으로 변경하면 그대로 유지한다`() {
        val nickname = Nickname("피카츄")
        val member = persistedMember(
            id = 42L,
            nickname = nickname,
            createdAt = Instant.parse("2026-07-31T03:30:00Z"),
        )
        val command = ChangeNicknameCommand(memberId = 42L, nickname = nickname.value)
        every { memberRepository.findById(command.memberId) } returns member

        memberService.change(command)

        member.nickname shouldBe nickname
        verify(exactly = 1) { memberRepository.findById(command.memberId) }
        verify(exactly = 0) { memberRepository.existsByNickname(any()) }
    }

    @Test
    fun `이미 사용 중인 닉네임으로 변경할 수 없다`() {
        val originalNickname = Nickname("피카츄")
        val member = persistedMember(
            id = 42L,
            nickname = originalNickname,
            createdAt = Instant.parse("2026-07-31T03:30:00Z"),
        )
        val command = ChangeNicknameCommand(memberId = 42L, nickname = "라이츄")
        every { memberRepository.findById(command.memberId) } returns member
        every { memberRepository.existsByNickname(command.nickname) } returns true

        val exception = shouldThrow<DomainException> {
            memberService.change(command)
        }

        exception.errorCode shouldBe MemberErrorCode.DUPLICATE_NICKNAME
        member.nickname shouldBe originalNickname
        verify(exactly = 1) { memberRepository.findById(command.memberId) }
        verify(exactly = 1) { memberRepository.existsByNickname(command.nickname) }
    }

    @Test
    fun `존재하지 않는 회원의 닉네임을 변경할 수 없다`() {
        val command = ChangeNicknameCommand(memberId = 42L, nickname = "라이츄")
        every { memberRepository.findById(command.memberId) } returns null

        val exception = shouldThrow<DomainException> {
            memberService.change(command)
        }

        exception.errorCode shouldBe MemberErrorCode.MEMBER_NOT_FOUND
        verify(exactly = 1) { memberRepository.findById(command.memberId) }
        verify(exactly = 0) { memberRepository.existsByNickname(any()) }
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
