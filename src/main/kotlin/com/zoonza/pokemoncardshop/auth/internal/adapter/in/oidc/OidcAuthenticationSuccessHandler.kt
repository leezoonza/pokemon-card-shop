package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.oidc

import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.IdentityTicketCookieManager
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.IdentityTicketUseCase
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OidcAuthenticationSuccessHandler(
    private val identityTicketUseCase: IdentityTicketUseCase,
    private val redirectProperties: RedirectProperties,
    private val cookieManager: IdentityTicketCookieManager,
) : AuthenticationSuccessHandler {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oidcUser = requireOidcUser(authentication.principal)

        val identity = VerifiedExternalIdentity(oidcUser.provider, oidcUser.subject)

        val result = identityTicketUseCase.issue(identity)

        val redirectUri = redirectUriFor(result.purpose)

        cookieManager.write(response, result.purpose, result.ticket)

        return response.sendRedirect(redirectUri)
    }

    private fun requireOidcUser(principal: Any?) =
        principal as? CustomOidcUser
            ?: throw OAuth2AuthenticationException(
                OAuth2Error(
                    "invalid_oidc_principal",
                    "OIDC 인증 사용자 정보를 확인할 수 없습니다.",
                    null,
                )
            )

    private fun redirectUriFor(purpose: IdentityTicketPurpose): String =
        when (purpose) {
            IdentityTicketPurpose.SIGNUP -> redirectProperties.signupUri
            IdentityTicketPurpose.LOGIN -> redirectProperties.loginUri
        }
}