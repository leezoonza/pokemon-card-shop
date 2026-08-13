package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.application.dto.LoginResult
import com.zoonza.pokemoncardshop.auth.internal.application.dto.RefreshResult
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.AuthenticationUseCase
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.test.fake.issuedAuthTokensFixture
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.global.exception.GlobalExceptionHandler
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockCookie
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AuthControllerTests {

    private val now = Instant.parse("2026-08-13T03:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val authenticationUseCase = mockk<AuthenticationUseCase>()
    private val refreshTokenCookieManager = mockk<RefreshTokenCookieManager>()
    private val identityTicketCookieManager = mockk<IdentityTicketCookieManager>()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(
            AuthController(
                clock = clock,
                authenticationUseCase = authenticationUseCase,
                refreshTokenCookieManager = refreshTokenCookieManager,
                identityTicketCookieManager = identityTicketCookieManager,
            ),
        )
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @Test
    fun `신원 티켓으로 로그인하고 액세스 토큰을 응답한다`() {
        val tokens = issuedAuthTokensFixture()
        every { authenticationUseCase.login("identity-ticket", now) } returns
                LoginResult(tokens, "ADMIN")
        every { identityTicketCookieManager.clear(any()) } just Runs
        every { refreshTokenCookieManager.write(any(), tokens.refreshToken) } just Runs

        mockMvc.perform(
            post("/api/auth/login")
                .cookie(MockCookie(IdentityTicketCookieManager.COOKIE_NAME, "identity-ticket")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andExpect(jsonPath("$.data.role").value("ADMIN"))

        verify(exactly = 1) { authenticationUseCase.login("identity-ticket", now) }
        verify(exactly = 1) { identityTicketCookieManager.clear(any()) }
        verify(exactly = 1) {
            refreshTokenCookieManager.write(any(), tokens.refreshToken)
        }
    }

    @Test
    fun `리프레시 토큰을 회전하고 새 액세스 토큰을 응답한다`() {
        val tokens = issuedAuthTokensFixture(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        every { authenticationUseCase.refresh("refresh-token") } returns
                RefreshResult(tokens, "ADMIN")
        every { refreshTokenCookieManager.write(any(), tokens.refreshToken) } just Runs

        mockMvc.perform(
            post("/api/auth/refresh")
                .cookie(MockCookie(RefreshTokenCookieManager.COOKIE_NAME, "refresh-token")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
            .andExpect(jsonPath("$.data.role").value("ADMIN"))

        verify(exactly = 1) { authenticationUseCase.refresh("refresh-token") }
        verify(exactly = 1) {
            refreshTokenCookieManager.write(any(), tokens.refreshToken)
        }
    }

    @Test
    fun `리프레시 토큰 쿠키가 없으면 인증 토큰 재발급을 거절한다`() {
        every { authenticationUseCase.refresh(null) } throws
                DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        mockMvc.perform(post("/api/auth/refresh"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data.code").value("AUTH-003"))

        verify(exactly = 1) { authenticationUseCase.refresh(null) }
        verify(exactly = 0) { refreshTokenCookieManager.write(any(), any()) }
    }

    @Test
    fun `리프레시 토큰을 폐기하고 쿠키를 만료시킨다`() {
        every { authenticationUseCase.logout("refresh-token") } just Runs
        every { refreshTokenCookieManager.clear(any()) } just Runs

        mockMvc.perform(
            post("/api/auth/logout")
                .cookie(MockCookie(RefreshTokenCookieManager.COOKIE_NAME, "refresh-token")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").doesNotExist())

        verify(exactly = 1) { authenticationUseCase.logout("refresh-token") }
        verify(exactly = 1) { refreshTokenCookieManager.clear(any()) }
    }

    @Test
    fun `리프레시 토큰 쿠키가 없어도 로그아웃한다`() {
        every { authenticationUseCase.logout(null) } just Runs
        every { refreshTokenCookieManager.clear(any()) } just Runs

        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").doesNotExist())

        verify(exactly = 1) { authenticationUseCase.logout(null) }
        verify(exactly = 1) { refreshTokenCookieManager.clear(any()) }
    }
}
