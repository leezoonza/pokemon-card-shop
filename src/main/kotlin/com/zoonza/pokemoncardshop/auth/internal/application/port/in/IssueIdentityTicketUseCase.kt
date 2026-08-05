package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedIdentityTicket
import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity

/**
 * 검증된 연동 계정 정보를 애플리케이션에서 사용할 일회성 티켓으로 교환한다.
 */
interface IssueIdentityTicketUseCase {
    /**
     * 연동 계정의 등록 여부에 따라 티켓의 용도를 결정하고 제한된 시간 동안 유효한 티켓을 발급한다.
     *
     * @param identity 외부 인증 제공자가 검증한 연동 계정 정보
     * @return 티켓의 용도와 발급된 일회성 티켓
     */
    fun issue(identity: VerifiedExternalIdentity): IssuedIdentityTicket
}
