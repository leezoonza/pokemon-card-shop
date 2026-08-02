package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class IdentityTicketCookieManager {
    fun write(
        response: HttpServletResponse,
        purpose: IdentityTicketPurpose,
        ticket: String
    ) {
        val cookie = ResponseCookie
            .from(COOKIE_NAME, ticket)
            .httpOnly(true)
//            .secure(true)
            .sameSite("Lax")
            .path(COOKIE_PATH)
            .maxAge(purpose.ttl)
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
        const val COOKIE_NAME: String = "IDENTITY_TICKET"
        private const val COOKIE_PATH = "/api/auth"
    }
}