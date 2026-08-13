package com.zoonza.pokemoncardshop.member.internal.application.port.`in`

import com.zoonza.pokemoncardshop.member.internal.application.service.MemberQueryService
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRepository
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class MemberQueryUseCaseTests {

    private val memberRepository = mockk<MemberRepository>()
    private val memberQueryUseCase: MemberQueryUseCase = MemberQueryService(memberRepository)

    @Test
    fun `등록되지 않은 닉네임은 사용할 수 있다`() {
        val nickname = Nickname("피카츄")
        every { memberRepository.existsByNickname(nickname) } returns false

        memberQueryUseCase.isNicknameAvailable(nickname) shouldBe true

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
    }

    @Test
    fun `등록된 닉네임은 사용할 수 없다`() {
        val nickname = Nickname("피카츄")
        every { memberRepository.existsByNickname(nickname) } returns true

        memberQueryUseCase.isNicknameAvailable(nickname) shouldBe false

        verify(exactly = 1) { memberRepository.existsByNickname(nickname) }
    }
}
