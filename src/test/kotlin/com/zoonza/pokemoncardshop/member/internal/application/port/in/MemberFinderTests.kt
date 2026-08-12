package com.zoonza.pokemoncardshop.member.internal.application.port.`in`

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.application.service.MemberQueryService
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

class MemberFinderTests {

    private val memberRepository = mockk<MemberRepository>()
    private val memberFinder: MemberFinder = MemberQueryService(memberRepository)

    @Test
    fun `식별자로 회원을 조회한다`() {
        val member = persistedMemberFixture()
        every { memberRepository.findById(42L) } returns member

        memberFinder.findById(42L) shouldBe member

        verify(exactly = 1) { memberRepository.findById(42L) }
    }

    @Test
    fun `존재하지 않는 회원을 필수 조회하면 오류가 발생한다`() {
        every { memberRepository.findById(42L) } returns null

        val exception = shouldThrow<DomainException> {
            memberFinder.findById(42L)
        }

        exception.errorCode shouldBe MemberErrorCode.MEMBER_NOT_FOUND
    }

    @Test
    fun `등록되지 않은 닉네임은 사용할 수 있다`() {
        val nickname = Nickname("피카츄")
        every { memberRepository.existsByNickname(nickname) } returns false

        memberFinder.isNicknameAvailable(nickname) shouldBe true

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
    }

    @Test
    fun `등록된 닉네임은 사용할 수 없다`() {
        val nickname = Nickname("피카츄")
        every { memberRepository.existsByNickname(nickname) } returns true

        memberFinder.isNicknameAvailable(nickname) shouldBe false

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
    }
}
