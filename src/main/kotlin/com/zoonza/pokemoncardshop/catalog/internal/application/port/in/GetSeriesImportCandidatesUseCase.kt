package com.zoonza.pokemoncardshop.catalog.internal.application.port.`in`

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesImportCandidateResult
import com.zoonza.pokemoncardshop.common.error.DomainException

/**
 * 외부 카탈로그에서 가져올 수 있는 시리즈 후보를 조회한다.
 */
interface GetSeriesImportCandidatesUseCase {
    /**
     * 로고가 있는 시리즈 후보와 각 후보의 등록 여부를 반환한다.
     *
     * @return 가져오기 가능한 시리즈 후보 목록
     * @throws DomainException 외부 카탈로그를 조회할 수 없거나 응답 데이터가 유효하지 않은 경우
     */
    fun getSeriesCandidates(): List<SeriesImportCandidateResult>
}
