package com.zoonza.pokemoncardshop.global.security

import com.zoonza.pokemoncardshop.TestcontainersConfiguration
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.web.cors.CorsConfigurationSource

@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class HttpSecurityIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val corsConfigurationSource: CorsConfigurationSource,
) {

    @Test
    fun `인증되지 않은 보호 경로는 표준 인증 오류를 반환한다`() {
        mockMvc.perform(get("/api/protected"))
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data.code").value("SECURITY-001"))
            .andExpect(jsonPath("$.data.message").value("인증이 필요합니다."))
            .andExpect(jsonPath("$.data.errors").isEmpty)
    }

    @Test
    fun `닉네임 확인 경로는 인증 없이 접근한다`() {
        mockMvc.perform(
            get("/api/members/nickname")
                .queryParam("nickname", "피카츄"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    @Test
    fun `허용한 출처에 자격 증명 요청을 허용한다`() {
        val request = MockHttpServletRequest("GET", "/api/members/nickname").apply {
            addHeader(HttpHeaders.ORIGIN, "http://localhost:3000")
        }

        val configuration = corsConfigurationSource.getCorsConfiguration(request)

        configuration?.allowedOrigins shouldContainExactly listOf("http://localhost:3000")
        configuration?.allowCredentials shouldBe true
    }
}
