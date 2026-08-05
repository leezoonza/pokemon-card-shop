package com.zoonza.pokemoncardshop.global.security

import com.zoonza.pokemoncardshop.common.error.ErrorCode

enum class SecurityErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int,
) : ErrorCode {
    AUTHENTICATION_REQUIRED(
        "SECURITY-001",
        "인증이 필요합니다.",
        401,
    ),

    ACCESS_DENIED(
        "SECURITY-002",
        "요청한 리소스에 접근할 권한이 없습니다.",
        403,
    ),
}
