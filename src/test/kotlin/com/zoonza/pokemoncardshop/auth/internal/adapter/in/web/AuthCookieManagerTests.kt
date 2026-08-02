package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedRefreshToken
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletResponse
import java.time.Duration

class AuthCookieManagerTests {

    private val identityTicketCookieManager = IdentityTicketCookieManager()
    private val refreshTokenCookieManager = RefreshTokenCookieManager()

    @Test
    fun `신원 티켓을 보호된 쿠키로 기록한다`() {
        val response = MockHttpServletResponse()

        identityTicketCookieManager.write(
            response,
            IdentityTicketPurpose.SIGNUP,
            "identity-ticket",
        )

        response.getHeader(HttpHeaders.SET_COOKIE)!!
            .shouldContain("IDENTITY_TICKET=identity-ticket")
            .shouldContain("Path=/api/auth")
            .shouldContain("Max-Age=600")
            .shouldContain("HttpOnly")
            .shouldContain("SameSite=Lax")
    }

    @Test
    fun `신원 티켓 쿠키를 만료시킨다`() {
        val response = MockHttpServletResponse()

        identityTicketCookieManager.clear(response)

        response.getHeader(HttpHeaders.SET_COOKIE)!!
            .shouldContain("IDENTITY_TICKET=")
            .shouldContain("Path=/api/auth")
            .shouldContain("Max-Age=0")
            .shouldContain("HttpOnly")
            .shouldContain("SameSite=Lax")
    }

    @Test
    fun `리프레시 토큰을 보호된 쿠키로 기록한다`() {
        val response = MockHttpServletResponse()

        refreshTokenCookieManager.write(
            response,
            IssuedRefreshToken("refresh-token", Duration.ofDays(14)),
        )

        response.getHeader(HttpHeaders.SET_COOKIE)!!
            .shouldContain("REFRESH_TOKEN=refresh-token")
            .shouldContain("Path=/api/auth")
            .shouldContain("Max-Age=1209600")
            .shouldContain("HttpOnly")
            .shouldContain("SameSite=Lax")
    }

    @Test
    fun `리프레시 토큰 쿠키를 만료시킨다`() {
        val response = MockHttpServletResponse()

        refreshTokenCookieManager.clear(response)

        response.getHeader(HttpHeaders.SET_COOKIE)!!
            .shouldContain("REFRESH_TOKEN=")
            .shouldContain("Path=/api/auth")
            .shouldContain("Max-Age=0")
            .shouldContain("HttpOnly")
            .shouldContain("SameSite=Lax")
    }
}
