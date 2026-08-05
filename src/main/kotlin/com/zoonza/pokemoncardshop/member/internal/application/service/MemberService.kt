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
    MemberLoginApi,
    MemberRoleQueryApi {

    override fun isAvailable(nickname: Nickname): Boolean {
        return !memberRepository.existsByNickname(nickname)
    }

    @Transactional
    override fun register(command: RegisterMemberCommand): RegisterMemberResult {
        val nickname = Nickname(command.nickname)

        validateUniqueNickname(nickname)

        val member = Member.register(
            nickname = nickname,
            role = MemberRole.MEMBER,
            createdAt = command.createdAt,
        )

        val saved = memberRepository.save(member)

        return RegisterMemberResult(saved.id, saved.role.value)
    }

    @Transactional
    override fun recordLogin(command: MemberLoginCommand): MemberLoginResult? {
        val member = memberRepository.findById(command.memberId)
            ?: return null

        member.recordLogin(command.loggedInAt)

        return MemberLoginResult(member.role.value)
    }

    override fun findByMemberId(memberId: Long): MemberRoleResult? {
        return memberRepository.findById(memberId)
            ?.let { MemberRoleResult(it.role.value) }
    }

    private fun validateUniqueNickname(nickname: Nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw DomainException(MemberErrorCode.DUPLICATE_NICKNAME)
        }
    }
}
