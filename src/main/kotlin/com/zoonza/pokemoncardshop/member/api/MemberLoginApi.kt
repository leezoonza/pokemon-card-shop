package com.zoonza.pokemoncardshop.member.api

/**
 * 다른 모듈에 회원 로그인 기록 기능을 제공한다.
 */
interface MemberLoginApi {
    /**
     * 회원의 마지막 로그인 시각을 갱신하고 현재 역할을 반환한다.
     *
     * @param command 로그인한 회원과 로그인 시각
     * @return 로그인한 회원의 역할. 회원이 존재하지 않으면 `null`
     */
    fun recordLogin(command: MemberLoginCommand): MemberLoginResult?
}
