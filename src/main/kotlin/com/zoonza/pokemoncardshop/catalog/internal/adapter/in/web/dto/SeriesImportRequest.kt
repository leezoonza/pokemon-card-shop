package com.zoonza.pokemoncardshop.catalog.internal.adapter.`in`.web.dto

import com.zoonza.pokemoncardshop.catalog.internal.application.port.dto.SeriesImportCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class SeriesImportRequest(
    @field:NotBlank(message = "시리즈를 선택해 주세요.")
    val seriesSourceId: String,

    @field:NotBlank(message = "시리즈의 한글명을 입력해 주세요.")
    val seriesNameKo: String,

    @field:NotNull(message = "시리즈의 출시일을 입력해 주세요.")
    val seriesReleaseDate: LocalDate,
) {
    fun toCommand(): SeriesImportCommand =
        SeriesImportCommand(
            seriesSourceId = seriesSourceId,
            seriesNameKo = seriesNameKo,
            seriesReleaseDate = seriesReleaseDate,
        )
}
