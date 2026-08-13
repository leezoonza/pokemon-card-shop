package com.zoonza.pokemoncardshop.member.internal.application.port.`in`

import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.internal.application.dto.MemberNicknameUpdateCommand

/**
 * 회원의 등록과 관련된 기능을 제공한다.
 */
interface MemberCommandUseCase {
    /**
     * 회원의 현재 닉네임을 요청한 닉네임으로 변경한다.
     *
     * 현재 닉네임과 요청한 닉네임이 같으면 변경 없이 성공한다.
     *
     * @param command 변경할 회원과 새로운 닉네임
     * @throws DomainException 회원이 존재하지 않거나 닉네임이 이미 사용 중인 경우
     */
    fun updateNickname(command: MemberNicknameUpdateCommand)
}
