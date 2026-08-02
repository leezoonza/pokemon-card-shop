package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.oidc

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OidcAuthenticationFailureHandler(
    private val redirectProperties: RedirectProperties,
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {
        val errorCode = (exception as? OAuth2AuthenticationException)
            ?.error
            ?.errorCode
            ?: DEFAULT_ERROR_CODE

        val redirectUri = UriComponentsBuilder
            .fromUriString(redirectProperties.failureUri)
            .queryParam(ERROR_PARAMETER, errorCode)
            .build()
            .encode()
            .toUriString()

        response.sendRedirect(redirectUri)
    }

    companion object {
        private const val ERROR_PARAMETER: String = "error"
        private const val DEFAULT_ERROR_CODE: String = "oidc_authentication_failed"
    }
}
