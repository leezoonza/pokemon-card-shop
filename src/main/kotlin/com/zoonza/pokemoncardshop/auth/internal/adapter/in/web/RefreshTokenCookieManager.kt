package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedRefreshToken
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RefreshTokenCookieManager() {
    fun write(
        response: HttpServletResponse,
        refreshToken: IssuedRefreshToken,
    ) {
        val cookie = ResponseCookie
            .from(COOKIE_NAME, refreshToken.value)
            .httpOnly(true)
//            .secure(true)
            .sameSite("Lax")
            .path(COOKIE_PATH)
            .maxAge(refreshToken.ttl)
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    fun clear(response: HttpServletResponse) {
        val cookie = ResponseCookie
            .from(COOKIE_NAME, "")
            .httpOnly(true)
//            .secure(true)
            .sameSite("Lax")
            .path(COOKIE_PATH)
            .maxAge(Duration.ZERO)
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }

    companion object {
        const val COOKIE_NAME = "REFRESH_TOKEN"
        private const val COOKIE_PATH = "/api/auth"
    }
}