package com.zoonza.pokemoncardshop.catalog.internal.domain.card

import com.zoonza.pokemoncardshop.common.error.ErrorCode

enum class CardErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int,
) : ErrorCode {

    ENGLISH_NAME_REQUIRED(
        "CARD-001",
        "카드의 영문명은 필수입니다.",
        400,
    ),

    NOT_SUPPORTED_CATEGORY(
        "CARD-002",
        "지원하지 않는 카드 카테고리 입니다.",
        400
    ),

    NOT_SUPPORTED_RARITY(
        "CARD-003",
        "지원하지 않는 카드 레어도 입니다.",
        400
    ),
}
