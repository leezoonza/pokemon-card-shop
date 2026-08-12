package com.zoonza.pokemoncardshop.member.api

import com.zoonza.pokemoncardshop.common.error.DomainException

/**
 * 다른 모듈에 회원 등록 기능을 제공한다.
 */
interface MemberRegistrationApi {
    /**
     * 닉네임을 검증해 기본 역할을 가진 회원을 등록한다.
     *
     * @param command 등록할 회원의 닉네임과 생성 시각
     * @return 등록된 회원의 식별자와 역할
     * @throws DomainException 닉네임이 유효하지 않거나 이미 사용 중인 경우
     */
    fun register(command: MemberRegisterCommand): MemberRegisterResult
}
