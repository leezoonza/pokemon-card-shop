package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionImportCandidateResult
import com.zoonza.pokemoncardshop.common.error.DomainException

/**
 * 외부 카탈로그에서 시리즈에 속한 확장팩 가져오기 후보를 조회한다.
 */
interface GetExpansionImportCandidatesUseCase {
    /**
     * 지정한 시리즈에 속하면서 로고가 있는 확장팩 후보와 각 후보의 등록 여부를 반환한다.
     *
     * @param seriesSourceId 외부 카탈로그의 시리즈 식별자
     * @return 가져오기 가능한 확장팩 후보 목록
     * @throws DomainException 외부 카탈로그를 조회할 수 없거나 시리즈 또는 응답 데이터가 유효하지 않은 경우
     */
    fun getExpansionCandidates(seriesSourceId: String): List<ExpansionImportCandidateResult>
}
