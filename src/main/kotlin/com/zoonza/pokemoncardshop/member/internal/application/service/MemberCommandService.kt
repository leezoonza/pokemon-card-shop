package com.zoonza.pokemoncardshop.member.internal.application.service

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.MemberRegisterCommand
import com.zoonza.pokemoncardshop.member.api.MemberRegisterResult
import com.zoonza.pokemoncardshop.member.api.MemberRegistrationApi
import com.zoonza.pokemoncardshop.member.internal.application.dto.UpdateNicknameCommand
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberFinder
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberRegister
import com.zoonza.pokemoncardshop.member.internal.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberCommandService(
    private val memberFinder: MemberFinder,
    private val memberRepository: MemberRepository
) : MemberRegister,
    MemberRegistrationApi {

    @Transactional
    override fun register(command: MemberRegisterCommand): MemberRegisterResult {
        val nickname = Nickname(command.nickname)

        validateUniqueNickname(nickname)

        val member = Member.register(
            nickname = nickname,
            role = MemberRole.MEMBER,
            createdAt = command.createdAt,
        )

        val saved = memberRepository.save(member)

        return MemberRegisterResult(saved.id, saved.role.value)
    }

    @Transactional
    override fun updateNickname(command: UpdateNicknameCommand) {
        val member = memberFinder.findById(command.memberId)

        if (member.nickname == command.nickname) return

        validateUniqueNickname(command.nickname)

        member.updateNickname(command.nickname)
    }

    private fun validateUniqueNickname(nickname: Nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw DomainException(MemberErrorCode.DUPLICATE_NICKNAME)
        }
    }
}