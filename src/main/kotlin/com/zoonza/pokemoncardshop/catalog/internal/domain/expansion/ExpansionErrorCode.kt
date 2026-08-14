package com.zoonza.pokemoncardshop.catalog.internal.domain.expansion

import com.zoonza.pokemoncardshop.common.error.ErrorCode

enum class ExpansionErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int,
) : ErrorCode {

    NAME_REQUIRED(
        "EXPANSION-001",
        "확장팩 이름은 필수입니다.",
        400,
    ),

    NEGATIVE_TOTAL_CARD_COUNT(
        "EXPANSION-003",
        "전체 카드 수는 0 이상이어야 합니다.",
        400,
    ),

    NEGATIVE_OFFICIAL_CARD_COUNT(
        "EXPANSION-004",
        "공식 카드 수는 0 이상이어야 합니다.",
        400,
    ),

    OFFICIAL_CARD_COUNT_EXCEEDS_TOTAL(
        "EXPANSION-005",
        "공식 카드 수는 전체 카드 수를 초과할 수 없습니다.",
        400,
    ),
}
