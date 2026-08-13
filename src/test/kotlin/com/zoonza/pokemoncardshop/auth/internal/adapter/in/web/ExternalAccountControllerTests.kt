package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupResult
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.ExternalAccountCommandUseCase
import com.zoonza.pokemoncardshop.auth.test.fake.issuedAuthTokensFixture
import com.zoonza.pokemoncardshop.global.exception.GlobalExceptionHandler
import io.mockk.*
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockCookie
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class ExternalAccountControllerTests {

    private val now = Instant.parse("2026-08-13T03:00:00Z")
    private val externalAccountCommandUseCase = mockk<ExternalAccountCommandUseCase>()
    private val refreshTokenCookieManager = mockk<RefreshTokenCookieManager>()
    private val identityTicketCookieManager = mockk<IdentityTicketCookieManager>()
    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(
            ExternalAccountController(
                clock = Clock.fixed(now, ZoneOffset.UTC),
                externalAccountCommandUseCase = externalAccountCommandUseCase,
                refreshTokenCookieManager = refreshTokenCookieManager,
                identityTicketCookieManager = identityTicketCookieManager,
            ),
        )
        .setControllerAdvice(GlobalExceptionHandler())
        .build()

    @Test
    fun `신원 티켓과 닉네임으로 가입하고 액세스 토큰을 응답한다`() {
        val command = SignupCommand("피카츄", "identity-ticket", now)
        val tokens = issuedAuthTokensFixture()
        every { externalAccountCommandUseCase.signup(command) } returns SignupResult(tokens, "MEMBER")
        every { identityTicketCookieManager.clear(any()) } just Runs
        every { refreshTokenCookieManager.write(any(), tokens.refreshToken) } just Runs

        mockMvc.perform(
            post("/api/external-accounts")
                .cookie(MockCookie(IdentityTicketCookieManager.COOKIE_NAME, "identity-ticket"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nickname":"피카츄"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").value("access-token"))
            .andExpect(jsonPath("$.data.role").value("MEMBER"))

        verify(exactly = 1) { externalAccountCommandUseCase.signup(command) }
        verify(exactly = 1) { identityTicketCookieManager.clear(any()) }
        verify(exactly = 1) { refreshTokenCookieManager.write(any(), tokens.refreshToken) }
    }

    @Test
    fun `올바르지 않은 닉네임은 가입 요청을 거절한다`() {
        mockMvc.perform(
            post("/api/external-accounts")
                .cookie(MockCookie(IdentityTicketCookieManager.COOKIE_NAME, "identity-ticket"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"nickname":"피"}"""),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.data.errors[0].field").value("nickname"))
            .andExpect(
                jsonPath("$.data.errors[0].message").value(
                    "닉네임은 2자 이상 14자 이하의 한글, 영문, 숫자로 입력해 주세요.",
                ),
            )

        verify(exactly = 0) { externalAccountCommandUseCase.signup(any()) }
        verify(exactly = 0) { identityTicketCookieManager.clear(any()) }
        verify(exactly = 0) { refreshTokenCookieManager.write(any(), any()) }
    }
}
