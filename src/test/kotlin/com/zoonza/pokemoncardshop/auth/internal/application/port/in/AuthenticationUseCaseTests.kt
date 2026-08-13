package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.LoginResult
import com.zoonza.pokemoncardshop.auth.internal.application.dto.RefreshResult
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenPort
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketPort
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenPort
import com.zoonza.pokemoncardshop.auth.internal.application.service.AuthenticationService
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountProvider
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import com.zoonza.pokemoncardshop.auth.test.fake.*
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.MemberAuthenticationApi
import com.zoonza.pokemoncardshop.member.api.MemberLoginCommand
import com.zoonza.pokemoncardshop.member.api.MemberLoginResult
import com.zoonza.pokemoncardshop.member.api.MemberRoleResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test

class AuthenticationUseCaseTests {

    private val authTokenPort = mockk<AuthTokenPort>()
    private val refreshTokenPort = mockk<RefreshTokenPort>()
    private val identityTicketPort = mockk<IdentityTicketPort>()
    private val memberAuthenticationApi = mockk<MemberAuthenticationApi>()
    private val externalAccountRepository = mockk<ExternalAccountRepository>()
    private val authenticationUseCase: AuthenticationUseCase = AuthenticationService(
        authTokenPort = authTokenPort,
        refreshTokenPort = refreshTokenPort,
        identityTicketPort = identityTicketPort,
        memberAuthenticationApi = memberAuthenticationApi,
        externalAccountRepository = externalAccountRepository,
    )

    @Test
    fun `가입한 연동 계정으로 로그인하고 인증 토큰을 발급한다`() {
        val identity = verifiedExternalIdentityFixture()
        val memberLoginCommand = slot<MemberLoginCommand>()
        val tokens = issuedAuthTokensFixture()
        every { identityTicketPort.consume("identity-ticket") } returns identity
        every {
            externalAccountRepository.findByProviderAndSubject(identity.provider, identity.subject)
        } returns externalIdentityFixture(identity = identity)
        every { memberAuthenticationApi.login(capture(memberLoginCommand)) } returns
                MemberLoginResult("MEMBER")
        every { authTokenPort.issue(42L, "MEMBER") } returns tokens
        every {
            refreshTokenPort.save(42L, "refresh-token", TEST_REFRESH_TOKEN_TTL)
        } just Runs

        val result = authenticationUseCase.login("identity-ticket", TEST_AUTHENTICATED_AT)

        result shouldBe LoginResult(tokens, "MEMBER")
        memberLoginCommand.captured shouldBe MemberLoginCommand(
            memberId = 42L,
            loggedInAt = TEST_AUTHENTICATED_AT,
        )
        verifyOrder {
            identityTicketPort.consume("identity-ticket")
            externalAccountRepository.findByProviderAndSubject(
                ExternalAccountProvider.GOOGLE,
                "google-subject",
            )
            memberAuthenticationApi.login(memberLoginCommand.captured)
            authTokenPort.issue(42L, "MEMBER")
            refreshTokenPort.save(42L, "refresh-token", TEST_REFRESH_TOKEN_TTL)
        }
    }

    @Test
    fun `가입하지 않은 연동 계정으로 로그인할 수 없다`() {
        val identity = verifiedExternalIdentityFixture().copy(subject = "unknown-subject")
        every { identityTicketPort.consume("identity-ticket") } returns identity
        every {
            externalAccountRepository.findByProviderAndSubject(identity.provider, identity.subject)
        } returns null

        val exception = shouldThrow<DomainException> {
            authenticationUseCase.login("identity-ticket", TEST_AUTHENTICATED_AT)
        }

        exception.errorCode shouldBe AuthErrorCode.AUTHENTICATION_FAILED
        verify(exactly = 0) {
            memberAuthenticationApi.login(any())
            authTokenPort.issue(any(), any())
            refreshTokenPort.save(any(), any(), any())
        }
    }

    @Test
    fun `연결 회원을 인증할 수 없으면 인증 실패로 변환한다`() {
        val identity = verifiedExternalIdentityFixture()
        every { identityTicketPort.consume("identity-ticket") } returns identity
        every {
            externalAccountRepository.findByProviderAndSubject(identity.provider, identity.subject)
        } returns externalIdentityFixture(identity = identity)
        every { memberAuthenticationApi.login(any()) } returns null

        val exception = shouldThrow<DomainException> {
            authenticationUseCase.login("identity-ticket", TEST_AUTHENTICATED_AT)
        }

        exception.errorCode shouldBe AuthErrorCode.AUTHENTICATION_FAILED
        verify(exactly = 0) {
            authTokenPort.issue(any(), any())
            refreshTokenPort.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰을 회전하고 인증 토큰을 다시 발급한다`() {
        val tokens = issuedAuthTokensFixture(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        every { refreshTokenPort.consume("refresh-token") } returns 42L
        every { memberAuthenticationApi.getMemberRole(42L) } returns MemberRoleResult("MEMBER")
        every { authTokenPort.issue(42L, "MEMBER") } returns tokens
        every {
            refreshTokenPort.save(42L, "new-refresh-token", TEST_REFRESH_TOKEN_TTL)
        } just Runs

        val result = authenticationUseCase.refresh("refresh-token")

        result shouldBe RefreshResult(tokens, "MEMBER")
        verifyOrder {
            refreshTokenPort.consume("refresh-token")
            memberAuthenticationApi.getMemberRole(42L)
            authTokenPort.issue(42L, "MEMBER")
            refreshTokenPort.save(42L, "new-refresh-token", TEST_REFRESH_TOKEN_TTL)
        }
    }

    @Test
    fun `유효하지 않은 리프레시 토큰으로 인증 토큰을 다시 발급할 수 없다`() {
        every { refreshTokenPort.consume("invalid-token") } throws
                DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        val exception = shouldThrow<DomainException> {
            authenticationUseCase.refresh("invalid-token")
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        verify(exactly = 0) {
            memberAuthenticationApi.getMemberRole(any())
            authTokenPort.issue(any(), any())
            refreshTokenPort.save(any(), any(), any())
        }
    }

    @Test
    fun `연결 회원을 조회할 수 없으면 리프레시 토큰 오류로 변환한다`() {
        every { refreshTokenPort.consume("refresh-token") } returns 42L
        every { memberAuthenticationApi.getMemberRole(42L) } returns null

        val exception = shouldThrow<DomainException> {
            authenticationUseCase.refresh("refresh-token")
        }

        exception.errorCode shouldBe AuthErrorCode.AUTHENTICATION_FAILED
        verify(exactly = 0) {
            authTokenPort.issue(any(), any())
            refreshTokenPort.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰이 없으면 인증 토큰을 다시 발급할 수 없다`() {
        listOf<String?>(null, " ").forEach { refreshToken ->
            val exception = shouldThrow<DomainException> {
                authenticationUseCase.refresh(refreshToken)
            }

            exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        }
        verify(exactly = 0) {
            refreshTokenPort.consume(any())
            memberAuthenticationApi.getMemberRole(any())
            authTokenPort.issue(any(), any())
            refreshTokenPort.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰을 삭제하고 로그아웃한다`() {
        every { refreshTokenPort.delete("refresh-token") } just Runs

        authenticationUseCase.logout("refresh-token")

        verify(exactly = 1) { refreshTokenPort.delete("refresh-token") }
    }

    @Test
    fun `리프레시 토큰이 없어도 로그아웃한다`() {
        authenticationUseCase.logout(null)
        authenticationUseCase.logout(" ")

        verify(exactly = 0) { refreshTokenPort.delete(any()) }
    }
}
