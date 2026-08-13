package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.LoginResponse
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.RefreshResponse
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.AuthenticationUseCase
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.Instant

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val clock: Clock,
    private val authenticationUseCase: AuthenticationUseCase,
    private val refreshTokenCookieManager: RefreshTokenCookieManager,
    private val identityTicketCookieManager: IdentityTicketCookieManager
) {
    @PostMapping("/login")
    fun login(
        response: HttpServletResponse,
        @CookieValue(IdentityTicketCookieManager.COOKIE_NAME) identityTicket: String
    ): ApiResponse<LoginResponse> {
        val result = authenticationUseCase.login(
            identityTicket = identityTicket,
            loggedInAt = Instant.now(clock)
        )

        identityTicketCookieManager.clear(response)
        refreshTokenCookieManager.write(response, result.tokens.refreshToken)

        return ApiResponse.success(LoginResponse(result))
    }

    @PostMapping("/refresh")
    fun refresh(
        response: HttpServletResponse,
        @CookieValue(name = RefreshTokenCookieManager.COOKIE_NAME, required = false) refreshToken: String?
    ): ApiResponse<RefreshResponse> {
        val result = authenticationUseCase.refresh(refreshToken)

        refreshTokenCookieManager.write(response, result.tokens.refreshToken)

        return ApiResponse.success(RefreshResponse(result))
    }

    @PostMapping("/logout")
    fun logout(
        response: HttpServletResponse,
        @CookieValue(name = RefreshTokenCookieManager.COOKIE_NAME, required = false) refreshToken: String?
    ): ApiResponse<Unit> {
        authenticationUseCase.logout(refreshToken)

        refreshTokenCookieManager.clear(response)

        return ApiResponse.success()
    }
}
