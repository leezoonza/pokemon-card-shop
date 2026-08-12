package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.AuthenticationResponse
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.SignupRequest
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.Authenticator
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticator: Authenticator,
    private val refreshTokenCookieManager: RefreshTokenCookieManager,
    private val identityTicketCookieManager: IdentityTicketCookieManager
) {
    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
        @CookieValue(IdentityTicketCookieManager.COOKIE_NAME) identityTicket: String,
        response: HttpServletResponse
    ): ApiResponse<AuthenticationResponse> {
        val command = SignupCommand(request.nickname, identityTicket)

        val result = authenticator.signup(command)

        identityTicketCookieManager.clear(response)
        refreshTokenCookieManager.write(response, result.tokens.refreshToken)

        return ApiResponse.success(
            AuthenticationResponse(
                accessToken = result.tokens.accessToken.value,
                role = result.role,
            ),
        )
    }

    @PostMapping("/login")
    fun login(
        @CookieValue(IdentityTicketCookieManager.COOKIE_NAME) identityTicket: String,
        response: HttpServletResponse
    ): ApiResponse<AuthenticationResponse> {
        val result = authenticator.authenticate(identityTicket)

        identityTicketCookieManager.clear(response)
        refreshTokenCookieManager.write(response, result.tokens.refreshToken)

        return ApiResponse.success(
            AuthenticationResponse(
                accessToken = result.tokens.accessToken.value,
                role = result.role,
            ),
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(
            name = RefreshTokenCookieManager.COOKIE_NAME,
            required = false,
        ) refreshToken: String?,
        response: HttpServletResponse,
    ): ApiResponse<AuthenticationResponse> {
        val result = authenticator.reissue(refreshToken)

        refreshTokenCookieManager.write(response, result.tokens.refreshToken)

        return ApiResponse.success(
            AuthenticationResponse(
                accessToken = result.tokens.accessToken.value,
                role = result.role,
            ),
        )
    }

    @PostMapping("/logout")
    fun logout(
        @CookieValue(
            name = RefreshTokenCookieManager.COOKIE_NAME,
            required = false
        ) refreshToken: String?,
        response: HttpServletResponse,
    ): ApiResponse<Unit> {
        authenticator.logout(refreshToken)

        refreshTokenCookieManager.clear(response)

        return ApiResponse.success()
    }
}
