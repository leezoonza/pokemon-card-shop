package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignupRequest(
    @field:NotBlank(message = "닉네임을 입력해 주세요.")
    @field:Pattern(
        regexp = "(?:[가-힣a-zA-Z0-9]{2,14})?",
        message = "닉네임은 2자 이상 14자 이하의 한글, 영문, 숫자로 입력해 주세요.",
    )
    val nickname: String
)
