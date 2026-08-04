package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.AuthTokenResponse
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.SignupRequest
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.LoginUseCase
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
    private val reissueAuthTokensUseCase: ReissueAuthTokensUseCase,
    private val refreshTokenCookieManager: RefreshTokenCookieManager,
    private val identityTicketCookieManager: IdentityTicketCookieManager
) {
    @PostMapping("/signup")
    fun signup(
        @Valid @RequestBody request: SignupRequest,
        @CookieValue(IdentityTicketCookieManager.COOKIE_NAME) identityTicket: String,
        response: HttpServletResponse
    ): ApiResponse<AuthTokenResponse> {
        val command = SignupCommand(request.nickname, identityTicket)

        val authTokens = signupUseCase.signup(command)

        identityTicketCookieManager.clear(response)
        refreshTokenCookieManager.write(response, authTokens.refreshToken)

        return ApiResponse.success(
            AuthTokenResponse(authTokens.accessToken.value)
        )
    }

    @PostMapping("/login")
    fun login(
        @CookieValue(IdentityTicketCookieManager.COOKIE_NAME) identityTicket: String,
        response: HttpServletResponse
    ): ApiResponse<AuthTokenResponse> {
        val authTokens = loginUseCase.login(identityTicket)

        identityTicketCookieManager.clear(response)
        refreshTokenCookieManager.write(response, authTokens.refreshToken)

        return ApiResponse.success(
            AuthTokenResponse(authTokens.accessToken.value)
        )
    }

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(
            name = RefreshTokenCookieManager.COOKIE_NAME,
            required = false,
        ) refreshToken: String?,
        response: HttpServletResponse,
    ): ApiResponse<AuthTokenResponse> {
        val authTokens = reissueAuthTokensUseCase.reissue(refreshToken)

        refreshTokenCookieManager.write(response, authTokens.refreshToken)

        return ApiResponse.success(
            AuthTokenResponse(authTokens.accessToken.value),
        )
    }
}
