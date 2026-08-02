package com.zoonza.pokemoncardshop.member.internal.application.service

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.MemberRegisterCommand
import com.zoonza.pokemoncardshop.member.api.MemberRegisterResult
import com.zoonza.pokemoncardshop.member.api.MemberRegistrationApi
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.CheckNicknameAvailabilityUseCase
import com.zoonza.pokemoncardshop.member.internal.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository
) : CheckNicknameAvailabilityUseCase,
    MemberRegistrationApi {

    override fun check(nickname: Nickname): Boolean {
        return !memberRepository.existsByNickname(nickname)
    }

    @Transactional
    override fun register(command: MemberRegisterCommand): MemberRegisterResult {
        val nickname = Nickname(command.nickname)

        validateUniqueNickname(nickname)

        val member = Member.register(
            nickname,
            MemberRole.MEMBER,
            command.createdAt,
        )

        val saved = memberRepository.save(member)

        return MemberRegisterResult(
            saved.id,
            saved.role.value
        )
    }

    private fun validateUniqueNickname(nickname: Nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw DomainException(MemberErrorCode.DUPLICATE_NICKNAME)
        }
    }
}
