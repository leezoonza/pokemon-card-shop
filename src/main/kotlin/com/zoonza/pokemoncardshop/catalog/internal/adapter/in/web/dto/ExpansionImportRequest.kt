package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionImportCommand
import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.ExpansionImportSelectionCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class ExpansionImportRequest(
    @field:Valid
    @field:NotEmpty(message = "등록할 확장팩을 하나 이상 선택해야 합니다.")
    val expansions: List<ExpansionImportSelection> = emptyList(),
) {
    fun toCommand(seriesSourceId: String): ExpansionImportCommand =
        ExpansionImportCommand(
            seriesSourceId = seriesSourceId,
            expansions = expansions.map { it.toCommand() }
        )
}


data class ExpansionImportSelection(
    @field:NotBlank(message = "확장팩 식별자는 비어 있을 수 없습니다.")
    val expansionSourceId: String,

    @field:NotBlank(message = "확장팩 한글명의 입력해 주세요.")
    val expansionNameKo: String,
) {
    fun toCommand(): ExpansionImportSelectionCommand =
        ExpansionImportSelectionCommand(
            expansionSourceId = expansionSourceId,
            expansionNameKo = expansionNameKo,
        )
}
