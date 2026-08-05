package com.zoonza.pokemoncardshop.global.security

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import tools.jackson.module.kotlin.jacksonObjectMapper

class ApiSecurityHandlersTests {

    private val responseWriter = ApiSecurityErrorResponseWriter(jacksonObjectMapper())

    @Test
    fun `인증되지 않은 요청을 표준 오류 응답으로 변환한다`() {
        val response = MockHttpServletResponse()
        val handler = ApiAuthenticationEntryPoint(responseWriter)

        handler.commence(
            MockHttpServletRequest(),
            response,
            mockk<AuthenticationException>(),
        )

        response.status shouldBe SecurityErrorCode.AUTHENTICATION_REQUIRED.status
        response.contentType.shouldStartWith(MediaType.APPLICATION_JSON_VALUE)
        response.contentAsString shouldBe
                """{"success":false,"data":{"code":"SECURITY-001","message":"인증이 필요합니다.","errors":[]}}"""
    }

    @Test
    fun `권한이 없는 요청을 표준 오류 응답으로 변환한다`() {
        val response = MockHttpServletResponse()
        val handler = ApiAccessDeniedHandler(responseWriter)

        handler.handle(
            MockHttpServletRequest(),
            response,
            AccessDeniedException("forbidden"),
        )

        response.status shouldBe SecurityErrorCode.ACCESS_DENIED.status
        response.contentType.shouldStartWith(MediaType.APPLICATION_JSON_VALUE)
        response.contentAsString shouldBe
                """{"success":false,"data":{"code":"SECURITY-002","message":"요청한 리소스에 접근할 권한이 없습니다.","errors":[]}}"""
    }
}
