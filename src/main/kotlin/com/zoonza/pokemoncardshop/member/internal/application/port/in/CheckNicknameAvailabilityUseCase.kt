package com.zoonza.pokemoncardshop.member.internal.application.port.`in`

import com.zoonza.pokemoncardshop.member.internal.domain.Nickname

/**
 * 회원 닉네임의 사용 가능 여부를 확인한다.
 */
interface CheckNicknameAvailabilityUseCase {
    /**
     * 동일한 닉네임을 사용하는 회원이 있는지 확인한다.
     *
     * @param nickname 확인할 유효한 형식의 닉네임
     * @return 닉네임을 사용할 수 있으면 `true`, 이미 사용 중이면 `false`
     */
    fun isAvailable(nickname: Nickname): Boolean
}
