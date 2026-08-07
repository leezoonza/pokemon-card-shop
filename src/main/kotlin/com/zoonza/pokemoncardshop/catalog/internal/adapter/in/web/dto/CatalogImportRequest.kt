package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.CatalogImportCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class CatalogImportRequest(
    @field:NotBlank(message = "시리즈를 선택해야 합니다.")
    val seriesSourceId: String = "",

    @field:NotNull(message = "시리즈 출시일을 입력해야 합니다.")
    val seriesReleaseDate: LocalDate? = null,

    @field:NotEmpty(message = "등록할 확장팩을 하나 이상 선택해야 합니다.")
    val expansionSourceIds: List<@NotBlank(message = "확장팩 식별자는 비어 있을 수 없습니다.") String> = emptyList(),
) {
    fun toCommand(): CatalogImportCommand = CatalogImportCommand(
        seriesSourceId = seriesSourceId,
        seriesReleaseDate = requireNotNull(seriesReleaseDate),
        expansionSourceIds = expansionSourceIds,
    )
}
