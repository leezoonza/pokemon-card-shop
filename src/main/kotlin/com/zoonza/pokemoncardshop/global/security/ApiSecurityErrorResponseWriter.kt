package com.zoonza.pokemoncardshop.global.security

import com.zoonza.pokemoncardshop.common.response.ApiResponse
import com.zoonza.pokemoncardshop.common.response.ErrorResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.nio.charset.StandardCharsets

@Component
class ApiSecurityErrorResponseWriter(
    private val objectMapper: ObjectMapper,
) {
    fun write(
        response: HttpServletResponse,
        errorCode: SecurityErrorCode,
    ) {
        response.status = errorCode.status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()

        val body = ApiResponse.failure(ErrorResponse.of(errorCode))

        objectMapper.writeValue(response.outputStream, body)
    }
}
