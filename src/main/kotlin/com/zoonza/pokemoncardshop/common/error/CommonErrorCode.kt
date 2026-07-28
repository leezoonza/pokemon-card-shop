package com.zoonza.pokemoncardshop.common.error

enum class CommonErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int,
) : ErrorCode {
    INTERNAL_SERVER_ERROR(
        "COMMON-001",
        "서버 내부 오류가 발생했습니다.",
        500
    ),

    VALIDATION_FAILED(
        "COMMON-002",
        "요청 값이 올바르지 않습니다.",
        400
    ),
    ;
}
