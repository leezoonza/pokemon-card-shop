package com.zoonza.pokemoncardshop.auth.internal.domain

import com.zoonza.pokemoncardshop.common.error.ErrorCode

enum class AuthErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int,
) : ErrorCode {

    UNSUPPORTED_IDENTITY_PROVIDER(
        "AUTH-001",
        "지원하지 않는 외부 인증 제공자입니다.",
        400
    ),

    INVALID_IDENTITY_TICKET(
        "AUTH-002",
        "인증 티켓이 유효하지 않거나 만료되었습니다.",
        401,
    ),

    INVALID_REFRESH_TOKEN(
        "AUTH-003",
        "리프레시 토큰이 유효하지 않거나 만료되었습니다.",
        401,
    ),

    EXTERNAL_IDENTITY_NOT_FOUND(
        "AUTH-004",
        "외부 인증 정보를 찾을 수 없습니다.",
        404,
    ),
}
