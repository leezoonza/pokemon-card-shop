package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedAuthTokens
import com.zoonza.pokemoncardshop.common.error.DomainException

/**
 * 연동 계정 인증 티켓으로 기존 회원을 인증한다.
 */
interface LoginUseCase {
    /**
     * 일회성 신원 티켓을 소비하고 연결된 회원의 인증 토큰을 발급한다.
     *
     * @param identityTicket 연동 계정 검증 후 발급된 로그인용 일회성 티켓
     * @return 새로 발급된 액세스 토큰과 리프레시 토큰
     * @throws DomainException 티켓이 유효하지 않거나 연동 계정에 연결된 회원을 찾을 수 없는 경우
     */
    fun login(identityTicket: String): IssuedAuthTokens
}
