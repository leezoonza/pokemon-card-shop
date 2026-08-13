package com.zoonza.pokemoncardshop.member.api

import com.zoonza.pokemoncardshop.common.error.DomainException

interface MemberAuthenticationApi {
    /**
     * 회원의 마지막 로그인 시각을 갱신하고 현재 역할을 반환한다.
     *
     * @param command 로그인한 회원과 로그인 시각
     * @return 로그인한 회원의 역할
     * @throws DomainException 연결된 회원이 존재하지 않거나 비활성화된 경우
     */
    fun login(command: MemberLoginCommand): MemberLoginResult?

    /**
     * 회원 식별자로 현재 역할을 조회한다.
     *
     * @param memberId 조회할 회원의 식별자
     * @return 회원의 현재 역할
     * @throws DomainException 연결된 회원이 존재하지 않는 경우
     */
    fun getMemberRole(memberId: Long): MemberRoleResult?
}
