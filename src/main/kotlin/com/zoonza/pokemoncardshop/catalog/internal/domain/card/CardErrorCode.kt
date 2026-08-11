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
}
