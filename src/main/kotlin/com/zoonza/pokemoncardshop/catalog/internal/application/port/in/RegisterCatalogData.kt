package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportResult
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogRegistrationPlan
import com.zoonza.pokemoncardshop.common.error.DomainException

/**
 * 검증과 변환이 끝난 카탈로그 데이터를 하나의 트랜잭션으로 등록한다.
 */
interface RegisterCatalogData {
    /**
     * 시리즈를 생성하거나 재사용하고 확장팩과 카드를 함께 등록한다.
     *
     * @param plan 등록할 시리즈, 확장팩과 카드 데이터
     * @return 등록된 시리즈 식별자와 확장팩 및 카드 수
     * @throws DomainException 기존 시리즈 정보가 일치하지 않거나 중복 데이터가 존재하는 경우
     */
    fun register(plan: CatalogRegistrationPlan): CatalogImportResult
}
