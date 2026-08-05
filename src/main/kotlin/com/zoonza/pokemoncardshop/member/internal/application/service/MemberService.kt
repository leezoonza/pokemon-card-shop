package com.zoonza.pokemoncardshop.member.internal.application.service

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.*
import com.zoonza.pokemoncardshop.member.internal.application.dto.ChangeNicknameCommand
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.ChangeNicknameUseCase
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
    MemberRoleQueryApi,
    ChangeNicknameUseCase {

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

    @Transactional
    override fun change(command: ChangeNicknameCommand) {
        val member = memberRepository.findById(command.memberId)
            ?: throw DomainException(MemberErrorCode.MEMBER_NOT_FOUND)

        if (member.nickname == command.nickname) return

        validateUniqueNickname(command.nickname)

        member.changeNickname(command.nickname)
    }

    private fun validateUniqueNickname(nickname: Nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw DomainException(MemberErrorCode.DUPLICATE_NICKNAME)
        }
    }
}
