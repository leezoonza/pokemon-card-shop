package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAuthTokens
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.common.error.DomainException

/**
 * 검증된 연동 계정으로 새 회원을 가입시킨다.
 */
interface SignupUseCase {
    /**
     * 가입용 일회성 인증 티켓을 소비해 회원과 연동 계정을 등록하고 인증 토큰을 발급한다.
     *
     * @param command 사용할 닉네임과 가입용 일회성 신원 티켓
     * @return 가입한 회원에게 새로 발급된 액세스 토큰과 리프레시 토큰
     * @throws DomainException 티켓이 유효하지 않거나 닉네임이 유효하지 않거나 이미 사용 중인 경우
     */
    fun signup(command: SignupCommand): IssuedAuthTokens
}
