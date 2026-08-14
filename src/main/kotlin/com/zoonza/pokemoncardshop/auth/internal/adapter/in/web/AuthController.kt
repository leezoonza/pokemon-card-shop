package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.LoginResponse
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.RefreshResponse
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.SignupRequest
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.SignupResponse
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.AuthenticationUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.ExternalAccountCommandUseCase
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.time.Clock
import java.time.Instant

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val clock: Clock,
    private val authenticationUseCase: AuthenticationUseCase,
    private val externalAccountCommandUseCase: ExternalAccountCommandUseCase,
    private val refreshTokenCookieManager: RefreshTokenCookieManager,
    private val identityTicketCookieManager: IdentityTicketCookieManager
) {
    @PostMapping("/signup")
    fun signup(
        response: HttpServletResponse,
        @Valid @RequestBody request: SignupRequest,
        @CookieValue(IdentityTicketCookieManager.COOKIE_NAME) identityTicket: String
    ): ApiResponse<SignupResponse> {
        val command = SignupCommand(
            nickname = request.nickname,
            identityTicket = identityTicket,
            createdAt = Instant.now(clock)
        )

        val result = externalAccountCommandUseCase.signup(command)

        identityTicketCookieManager.clear(response)
        refreshTokenCookieManager.write(response, result.tokens.refreshToken)

        return ApiResponse.success(SignupResponse(result))
    }

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
