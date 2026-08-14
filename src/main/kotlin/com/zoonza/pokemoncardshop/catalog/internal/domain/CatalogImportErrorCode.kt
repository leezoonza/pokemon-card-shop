package com.zoonza.pokemoncardshop.catalog.internal.domain

import com.zoonza.pokemoncardshop.common.error.ErrorCode

enum class CatalogImportErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int,
) : ErrorCode {
    DUPLICATE_EXPANSION_SELECTION(
        "CATALOG-IMPORT-002",
        "동일한 확장팩을 중복으로 선택할 수 없습니다.",
        400,
    ),

    EMPTY_EXPANSION_SELECTION(
        "CATALOG-IMPORT-003",
        "등록할 확장팩을 하나 이상 선택해야 합니다.",
        400,
    ),

    EXPANSION_ALREADY_REGISTERED(
        "CATALOG-IMPORT-004",
        "이미 등록된 확장팩입니다.",
        409,
    ),

    EXPANSION_LOGO_REQUIRED(
        "CATALOG-IMPORT-006",
        "로고가 없는 확장팩은 등록할 수 없습니다.",
        400,
    ),

    SERIES_NOT_REGISTERED(
        "CATALOG-IMPORT-007",
        "등록되지 않은 시리즈입니다. 시리즈를 먼저 등록해 주세요.",
        400,
    ),

    SOURCE_DATA_ALREADY_REGISTERED(
        "CATALOG-IMPORT-008",
        "이미 등록된 카탈로그 데이터입니다.",
        409,
    ),

    SOURCE_DATA_INVALID(
        "CATALOG-IMPORT-009",
        "외부 카탈로그 데이터 형식이 올바르지 않습니다.",
        502,
    ),

    SOURCE_DATA_NOT_FOUND(
        "CATALOG-IMPORT-010",
        "외부 카탈로그에서 요청한 데이터를 찾을 수 없습니다.",
        404,
    ),

    SOURCE_UNAVAILABLE(
        "CATALOG-IMPORT-011",
        "외부 카탈로그를 일시적으로 조회할 수 없습니다.",
        503,
    ),
}
