package com.zoonza.pokemoncardshop.member.internal.application.service

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberFinder
import com.zoonza.pokemoncardshop.member.internal.domain.Member
import com.zoonza.pokemoncardshop.member.internal.domain.MemberErrorCode
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRepository
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import org.springframework.stereotype.Service

@Service
class MemberQueryService(
    private val memberRepository: MemberRepository
) : MemberFinder {

    override fun findById(memberId: Long): Member {
        return memberRepository.findById(memberId)
            ?: throw DomainException(MemberErrorCode.MEMBER_NOT_FOUND)
    }

    override fun isNicknameAvailable(nickname: Nickname): Boolean {
        return !memberRepository.existsByNickname(nickname)
    }
}
