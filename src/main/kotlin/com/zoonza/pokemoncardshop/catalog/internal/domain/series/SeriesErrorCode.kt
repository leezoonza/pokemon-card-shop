package com.zoonza.pokemoncardshop.catalog.internal.domain.series

import com.zoonza.pokemoncardshop.common.error.ErrorCode

enum class SeriesErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int,
) : ErrorCode {

    ENGLISH_NAME_REQUIRED(
        "SERIES-001",
        "시리즈의 영문명은 필수입니다.",
        400,
    ),

    KOREAN_NAME_REQUIRED(
        "SERIES-002",
        "시리즈의 한글명은 필수입니다.",
        400,
    ),
}
