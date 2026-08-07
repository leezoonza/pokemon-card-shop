package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.AuthenticationResponse
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.SignupRequest
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.LoginUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.LogoutUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.ReissueAuthTokensUseCase
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.SignupUseCase
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val signupUseCase: SignupUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val reissueAuthTokensUseCase: ReissueAuthTokensUseCase,
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

        val result = signupUseCase.signup(command)

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
        val result = loginUseCase.login(identityTicket)

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
        val result = reissueAuthTokensUseCase.reissue(refreshToken)

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
        logoutUseCase.logout(refreshToken)

        refreshTokenCookieManager.clear(response)

        return ApiResponse.success()
    }
}
