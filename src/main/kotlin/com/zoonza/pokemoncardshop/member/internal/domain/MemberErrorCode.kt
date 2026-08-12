package com.zoonza.pokemoncardshop.member.internal.domain

import com.zoonza.pokemoncardshop.common.error.ErrorCode

enum class MemberErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int
) : ErrorCode {

    INVALID_NICKNAME(
        "MEMBER-001",
        "닉네임은 2자 이상 14자 이하의 한글, 영문, 숫자로 입력해 주세요.",
        400
    ),

    DUPLICATE_NICKNAME(
        "MEMBER-002",
        "이미 사용 중인 닉네임입니다.",
        409
    ),

    MEMBER_NOT_FOUND(
        "MEMBER-003",
        "회원 정보를 찾을 수 없습니다.",
        404,
    ),

    DEACTIVATED_MEMBER(
        "MEMBER-004",
        "비활성화된 회원입니다.",
        403,
    ),
}
