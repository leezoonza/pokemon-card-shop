package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportCommand
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportResult
import com.zoonza.pokemoncardshop.common.error.DomainException

/**
 * 외부 카탈로그의 시리즈와 선택한 확장팩을 서비스 카탈로그로 가져온다.
 */
interface ImportCatalogUseCase {
    /**
     * 시리즈, 선택한 확장팩과 소속 카드를 검증한 뒤 하나의 카탈로그로 등록한다.
     *
     * 이미 등록된 시리즈는 출시일이 일치할 때 재사용하고, 선택한 확장팩과 카드는 새로 등록한다.
     *
     * @param command 가져올 시리즈와 확장팩 및 시리즈 출시일
     * @return 등록된 시리즈 식별자와 확장팩 및 카드 수
     * @throws DomainException 선택 또는 외부 데이터가 유효하지 않거나 중복 데이터가 존재하는 경우
     */
    fun importCatalog(command: CatalogImportCommand): CatalogImportResult
}
