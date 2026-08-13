package com.zoonza.pokemoncardshop.member.internal.application.service

import com.zoonza.pokemoncardshop.member.internal.application.port.`in`.MemberQueryUseCase
import com.zoonza.pokemoncardshop.member.internal.domain.MemberRepository
import com.zoonza.pokemoncardshop.member.internal.domain.Nickname
import org.springframework.stereotype.Service

@Service
class MemberQueryService(
    private val memberRepository: MemberRepository
) : MemberQueryUseCase {

    override fun isNicknameAvailable(nickname: Nickname): Boolean {
        return !memberRepository.existsByNickname(nickname)
    }
}
