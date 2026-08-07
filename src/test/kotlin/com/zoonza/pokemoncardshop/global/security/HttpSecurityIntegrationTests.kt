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
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
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
        mockMvc.get("/api/protected")
            .andExpect {
                status { isUnauthorized() }
                content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                jsonPath("$.success") { value(false) }
                jsonPath("$.data.code") { value("SECURITY-001") }
                jsonPath("$.data.message") { value("인증이 필요합니다.") }
                jsonPath("$.data.errors") { isEmpty() }
            }
    }

    @Test
    fun `닉네임 확인 경로는 인증 없이 접근한다`() {
        mockMvc.get("/api/members/nickname") {
            queryParam("nickname", "피카츄")
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
            }
    }

    @Test
    fun `일반 회원은 관리자 API에 접근할 수 없다`() {
        mockMvc.get("/api/admin/catalog/imports") {
            with(jwt().authorities(SimpleGrantedAuthority("ROLE_MEMBER")))
        }
            .andExpect {
                status { isForbidden() }
                jsonPath("$.success") { value(false) }
                jsonPath("$.data.code") { value("SECURITY-002") }
            }
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
