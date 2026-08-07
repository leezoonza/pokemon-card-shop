package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthenticationResult
import com.zoonza.pokemoncardshop.common.error.DomainException

/**
 * 유효한 리프레시 토큰으로 인증 토큰을 갱신한다.
 */
interface ReissueAuthTokensUseCase {
    /**
     * 리프레시 토큰을 일회성으로 소비하고 새로운 인증 토큰을 발급한다.
     *
     * 발급에 성공하면 기존 리프레시 토큰은 더 이상 사용할 수 없다.
     *
     * @param refreshToken 재발급에 사용할 리프레시 토큰
     * @return 새로 발급된 인증 토큰과 인증된 회원의 역할
     * @throws DomainException 토큰이 없거나 비어 있거나 유효하지 않은 경우
     */
    fun reissue(refreshToken: String?): AuthenticationResult
}
