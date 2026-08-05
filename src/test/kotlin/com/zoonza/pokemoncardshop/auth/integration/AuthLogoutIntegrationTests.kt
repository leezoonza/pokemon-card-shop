package com.zoonza.pokemoncardshop.auth.integration

import com.zoonza.pokemoncardshop.TestcontainersConfiguration
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.RefreshTokenCookieManager
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.common.error.DomainException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockCookie
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Duration

@Import(TestcontainersConfiguration::class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AuthLogoutIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val refreshTokenStore: RefreshTokenStore,
) {

    @Test
    fun `리프레시 토큰을 폐기하고 로그아웃한다`() {
        refreshTokenStore.save(42L, "logout-refresh-token", Duration.ofDays(14))

        val result = mockMvc.post("/api/auth/logout") {
            with(csrf())
            cookie(
                MockCookie(
                    RefreshTokenCookieManager.COOKIE_NAME,
                    "logout-refresh-token",
                ),
            )
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data") { doesNotExist() }
            }
            .andReturn()

        result.response.getHeaders(HttpHeaders.SET_COOKIE)
            .single { it.startsWith("${RefreshTokenCookieManager.COOKIE_NAME}=") }
            .shouldContain("Max-Age=0")

        val exception = shouldThrow<DomainException> {
            refreshTokenStore.consume("logout-refresh-token")
        }
        exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
    }

    @Test
    fun `리프레시 토큰이 없어도 로그아웃한다`() {
        val result = mockMvc.post("/api/auth/logout") {
            with(csrf())
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data") { doesNotExist() }
            }
            .andReturn()

        result.response.getHeaders(HttpHeaders.SET_COOKIE)
            .single { it.startsWith("${RefreshTokenCookieManager.COOKIE_NAME}=") }
            .shouldContain("Max-Age=0")
    }
}
