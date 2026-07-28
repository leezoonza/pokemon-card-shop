package com.zoonza.pokemoncardshop.common.response

import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

class ApiResponseTests {

    @Test
    fun `API 응답은 기존 JSON 필드명을 유지한다`() {
        val objectMapper = JsonMapper.builder()
            .addModule(KotlinModule.Builder().build())
            .build()

        val json = objectMapper.writeValueAsString(ApiResponse.success("data"))

        json shouldContain "\"success\":true"
        json shouldNotContain "\"isSuccess\""
    }
}
