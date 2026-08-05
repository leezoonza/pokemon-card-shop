package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

/**
 * 회원의 리프레시 토큰을 폐기해 인증을 종료한다.
 */
interface LogoutUseCase {
    /**
     * 전달된 리프레시 토큰을 폐기한다.
     *
     * 토큰이 없거나 비어 있거나 이미 존재하지 않아도 정상적으로 종료한다.
     *
     * @param refreshToken 폐기할 리프레시 토큰
     */
    fun logout(refreshToken: String?)
}
