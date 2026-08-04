package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.oidc

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error

class OidcAuthenticationFailureHandlerTests {

    private val handler = OidcAuthenticationFailureHandler(
        RedirectProperties(
            signupUri = "http://localhost:3000/signup",
            loginUri = "http://localhost:3000/login",
            failureUri = "http://localhost:3000/login/error",
        ),
    )

    @Test
    fun `OIDC 오류 코드를 포함한 실패 페이지로 이동한다`() {
        val response = MockHttpServletResponse()
        val exception = OAuth2AuthenticationException(
            OAuth2Error("invalid_id_token"),
        )

        handler.onAuthenticationFailure(
            MockHttpServletRequest(),
            response,
            exception,
        )

        response.redirectedUrl shouldBe
                "http://localhost:3000/login/error?error=invalid_id_token"
    }

    @Test
    fun `OIDC 오류가 아니면 기본 오류 코드를 포함한 실패 페이지로 이동한다`() {
        val response = MockHttpServletResponse()

        handler.onAuthenticationFailure(
            MockHttpServletRequest(),
            response,
            BadCredentialsException("인증에 실패했습니다."),
        )

        response.redirectedUrl shouldBe
                "http://localhost:3000/login/error?error=oidc_authentication_failed"
    }
}
