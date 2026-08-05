package com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.oidc

import com.zoonza.pokemoncardshop.auth.internal.adapter.`in`.web.IdentityTicketCookieManager
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import com.zoonza.pokemoncardshop.auth.internal.application.dto.IssuedIdentityTicket
import com.zoonza.pokemoncardshop.auth.internal.application.dto.VerifiedExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.application.port.`in`.IssueIdentityTicketUseCase
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import java.util.stream.Stream

class OidcAuthenticationSuccessHandlerTests {

    private val cookieManager = mockk<IdentityTicketCookieManager>()
    private val issueIdentityTicketUseCase = mockk<IssueIdentityTicketUseCase>()
    private val handler = OidcAuthenticationSuccessHandler(
        RedirectProperties(
            signupUri = SIGNUP_URI,
            loginUri = LOGIN_URI,
            failureUri = "http://localhost:3000/login/error",
        ),
        cookieManager,
        issueIdentityTicketUseCase,
    )

    @ParameterizedTest
    @MethodSource("redirectCases")
    fun `신원 티켓 목적에 맞는 페이지로 이동한다`(
        purpose: IdentityTicketPurpose,
        redirectUri: String,
    ) {
        val identity = VerifiedExternalIdentity(IdentityProvider.GOOGLE, "google-subject")
        val authentication = mockk<Authentication>()
        val oidcUser = CustomOidcUser(
            mockk<OidcUser>(),
            identity.provider,
            identity.subject,
        )
        every { authentication.principal } returns oidcUser
        every { issueIdentityTicketUseCase.issue(identity) } returns
                IssuedIdentityTicket(purpose, "identity-ticket")
        every {
            cookieManager.write(any(), purpose, "identity-ticket")
        } just Runs

        val response = MockHttpServletResponse()

        handler.onAuthenticationSuccess(
            MockHttpServletRequest(),
            response,
            authentication,
        )

        response.redirectedUrl shouldBe redirectUri

        verify(exactly = 1) { issueIdentityTicketUseCase.issue(identity) }
        verify(exactly = 1) { cookieManager.write(response, purpose, "identity-ticket") }
    }

    @Test
    fun `OIDC 사용자가 아니면 인증 성공을 처리할 수 없다`() {
        val authentication = mockk<Authentication>()

        every { authentication.principal } returns "invalid-principal"

        val exception = shouldThrow<OAuth2AuthenticationException> {
            handler.onAuthenticationSuccess(
                MockHttpServletRequest(),
                MockHttpServletResponse(),
                authentication,
            )
        }

        exception.error.errorCode shouldBe "invalid_oidc_principal"

        verify(exactly = 0) { issueIdentityTicketUseCase.issue(any()) }
        verify(exactly = 0) { cookieManager.write(any(), any(), any()) }
    }

    companion object {
        private const val SIGNUP_URI = "http://localhost:3000/signup"
        private const val LOGIN_URI = "http://localhost:3000/login"

        @JvmStatic
        fun redirectCases(): Stream<Arguments> = Stream.of(
            Arguments.of(IdentityTicketPurpose.SIGNUP, SIGNUP_URI),
            Arguments.of(IdentityTicketPurpose.LOGIN, LOGIN_URI),
        )
    }
}
