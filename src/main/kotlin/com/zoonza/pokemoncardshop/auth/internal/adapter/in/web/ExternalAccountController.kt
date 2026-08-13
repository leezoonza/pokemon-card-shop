package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web

import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.SignupRequest
import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.dto.SignupResponse
import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupCommand
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.ExternalAccountCommandUseCase
import com.zoonza.pokemoncardshop.common.response.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.time.Clock
import java.time.Instant

@RestController
@RequestMapping("/api/external-accounts")
class ExternalAccountController(
    private val clock: Clock,
    private val externalAccountCommandUseCase: ExternalAccountCommandUseCase,
    private val refreshTokenCookieManager: RefreshTokenCookieManager,
    private val identityTicketCookieManager: IdentityTicketCookieManager
) {
    @PostMapping
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
}
