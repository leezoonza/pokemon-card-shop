package com.zoonza.pokemoncardshop.member.api

/**
 * 다른 모듈에 회원 역할 조회 기능을 제공한다.
 */
interface MemberRoleQueryApi {
    /**
     * 회원 식별자로 현재 역할을 조회한다.
     *
     * @param memberId 조회할 회원의 식별자
     * @return 회원의 현재 역할. 회원이 존재하지 않으면 `null`
     */
    fun findByMemberId(memberId: Long): MemberRoleResult?
}
