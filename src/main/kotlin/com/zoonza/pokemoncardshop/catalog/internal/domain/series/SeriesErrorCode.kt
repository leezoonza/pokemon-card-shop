package com.zoonza.pokemoncardshop.catalog.internal.domain.series

import com.zoonza.pokemoncardshop.common.error.ErrorCode

enum class SeriesErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int,
) : ErrorCode {

    NAME_REQUIRED(
        "SERIES-001",
        "시리즈 이름은 필수입니다.",
        400,
    ),
}
