package com.zoonza.pokemoncardshop.member.internal.application.port.`in`

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.application.dto.UpdateNicknameCommand
import com.zoonza.pokemoncardshop.member.internal.application.service.MemberCommandService
import com.zoonza.pokemoncardshop.member.internal.domain.MemberErrorCode
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRepository
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import com.zoonza.pokemoncardshop.member.test.fake.persistedMemberFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class MemberRegisterTests {

    private val memberFinder = mockk<MemberFinder>()
    private val memberRepository = mockk<MemberRepository>()
    private val memberRegister: MemberRegister = MemberCommandService(
        memberFinder = memberFinder,
        memberRepository = memberRepository,
    )

    @Test
    fun `사용 가능한 닉네임으로 변경한다`() {
        val member = persistedMemberFixture()
        val command = UpdateNicknameCommand(memberId = 42L, nickname = "라이츄")
        every { memberFinder.findById(42L) } returns member
        every { memberRepository.existsByNickname(command.nickname) } returns false

        memberRegister.updateNickname(command)

        member.nickname shouldBe command.nickname
        verify(exactly = 1) {
            memberFinder.findById(42L)
            memberRepository.existsByNickname(command.nickname)
        }
    }

    @Test
    fun `현재 닉네임으로 변경하면 그대로 유지한다`() {
        val nickname = Nickname("피카츄")
        val member = persistedMemberFixture(nickname = nickname)
        val command = UpdateNicknameCommand(memberId = 42L, nickname = nickname.value)
        every { memberFinder.findById(42L) } returns member

        memberRegister.updateNickname(command)

        member.nickname shouldBe nickname
        verify(exactly = 0) { memberRepository.existsByNickname(any()) }
    }

    @Test
    fun `이미 사용 중인 닉네임으로 변경할 수 없다`() {
        val originalNickname = Nickname("피카츄")
        val member = persistedMemberFixture(nickname = originalNickname)
        val command = UpdateNicknameCommand(memberId = 42L, nickname = "라이츄")
        every { memberFinder.findById(42L) } returns member
        every { memberRepository.existsByNickname(command.nickname) } returns true

        val exception = shouldThrow<DomainException> {
            memberRegister.updateNickname(command)
        }

        exception.errorCode shouldBe MemberErrorCode.DUPLICATE_NICKNAME
        member.nickname shouldBe originalNickname
        verify(exactly = 1) { memberRepository.existsByNickname(command.nickname) }
    }

    @Test
    fun `존재하지 않는 회원의 닉네임을 변경할 수 없다`() {
        val command = UpdateNicknameCommand(memberId = 42L, nickname = "라이츄")
        every { memberFinder.findById(42L) } throws
                DomainException(MemberErrorCode.MEMBER_NOT_FOUND)

        val exception = shouldThrow<DomainException> {
            memberRegister.updateNickname(command)
        }

        exception.errorCode shouldBe MemberErrorCode.MEMBER_NOT_FOUND
        verify(exactly = 0) { memberRepository.existsByNickname(any()) }
    }
}
