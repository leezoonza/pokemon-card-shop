package com.zoonza.pokemoncardshop.member.internal.application.service

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.*
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.CheckNicknameAvailabilityUseCase
import com.zoonza.pokemoncardshop.member.internal.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository
) : CheckNicknameAvailabilityUseCase,
    MemberRegistrationApi,
    MemberLoginApi {

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

    @Transactional
    override fun recordLogin(command: MemberLoginCommand): MemberLoginResult {
        val member = memberRepository
            .findById(command.memberId)
            ?: throw DomainException(MemberErrorCode.MEMBER_NOT_FOUND)

        member.recordLogin(command.loggedInAt)

        return MemberLoginResult(member.role.value)
    }

    private fun validateUniqueNickname(nickname: Nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw DomainException(MemberErrorCode.DUPLICATE_NICKNAME)
        }
    }
}
