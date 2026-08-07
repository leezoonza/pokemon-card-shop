package com.zoonza.pokemoncardshop.catalog.internal.domain

import com.zoonza.pokemoncardshop.common.error.ErrorCode

enum class CatalogImportErrorCode(
    override val code: String,
    override val message: String,
    override val status: Int,
) : ErrorCode {
    EMPTY_EXPANSION_SELECTION(
        "CATALOG-IMPORT-001",
        "등록할 확장팩을 하나 이상 선택해야 합니다.",
        400,
    ),
    DUPLICATE_EXPANSION_SELECTION(
        "CATALOG-IMPORT-002",
        "동일한 확장팩을 중복으로 선택할 수 없습니다.",
        400,
    ),
    SERIES_LOGO_REQUIRED(
        "CATALOG-IMPORT-003",
        "로고가 없는 시리즈는 등록할 수 없습니다.",
        400,
    ),
    EXPANSION_LOGO_REQUIRED(
        "CATALOG-IMPORT-004",
        "로고가 없는 확장팩은 등록할 수 없습니다.",
        400,
    ),
    EXPANSION_NOT_IN_SERIES(
        "CATALOG-IMPORT-005",
        "선택한 시리즈에 속하지 않은 확장팩이 포함되어 있습니다.",
        400,
    ),
    EXPANSION_ALREADY_REGISTERED(
        "CATALOG-IMPORT-006",
        "이미 등록된 확장팩이 포함되어 있습니다.",
        409,
    ),
    SERIES_RELEASE_DATE_MISMATCH(
        "CATALOG-IMPORT-007",
        "기존 시리즈의 출시일과 입력한 출시일이 일치하지 않습니다.",
        409,
    ),
    CARD_NOT_IN_EXPANSION(
        "CATALOG-IMPORT-008",
        "확장팩에 속하지 않은 카드가 포함되어 있습니다.",
        502,
    ),
    CARD_IMAGE_REQUIRED(
        "CATALOG-IMPORT-009",
        "이미지가 없는 카드는 등록할 수 없습니다.",
        502,
    ),
    UNSUPPORTED_CARD_CATEGORY(
        "CATALOG-IMPORT-010",
        "지원하지 않는 카드 카테고리가 포함되어 있습니다.",
        502,
    ),
    UNSUPPORTED_CARD_RARITY(
        "CATALOG-IMPORT-011",
        "지원하지 않는 카드 희귀도가 포함되어 있습니다.",
        502,
    ),
    SOURCE_DATA_NOT_FOUND(
        "CATALOG-IMPORT-012",
        "외부 카탈로그에서 요청한 데이터를 찾을 수 없습니다.",
        404,
    ),
    SOURCE_UNAVAILABLE(
        "CATALOG-IMPORT-013",
        "외부 카탈로그를 일시적으로 조회할 수 없습니다.",
        503,
    ),
    SOURCE_DATA_INVALID(
        "CATALOG-IMPORT-014",
        "외부 카탈로그 데이터 형식이 올바르지 않습니다.",
        502,
    ),
    DUPLICATE_SOURCE_DATA(
        "CATALOG-IMPORT-015",
        "이미 등록된 카탈로그 데이터가 포함되어 있습니다.",
        409,
    ),
}
