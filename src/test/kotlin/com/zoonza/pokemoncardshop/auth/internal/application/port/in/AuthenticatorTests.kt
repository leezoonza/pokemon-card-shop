package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.AuthenticationResult
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenIssuer
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.application.service.AuthenticationService
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentityRepository
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import com.zoonza.pokemoncardshop.auth.test.fake.*
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.*
import com.zoonza.pokemoncardshop.member.internal.domain.MemberErrorCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.ZoneOffset

class AuthenticatorTests {

    private val authTokenIssuer = mockk<AuthTokenIssuer>()
    private val refreshTokenStore = mockk<RefreshTokenStore>()
    private val identityTicketStore = mockk<IdentityTicketStore>()
    private val memberRegistrationApi = mockk<MemberRegistrationApi>()
    private val memberAuthenticationApi = mockk<MemberAuthenticationApi>()
    private val externalIdentityRepository = mockk<ExternalIdentityRepository>()
    private val authenticator: Authenticator = AuthenticationService(
        clock = Clock.fixed(TEST_AUTHENTICATED_AT, ZoneOffset.UTC),
        authTokenIssuer = authTokenIssuer,
        refreshTokenStore = refreshTokenStore,
        identityTicketStore = identityTicketStore,
        memberRegistrationApi = memberRegistrationApi,
        memberAuthenticationApi = memberAuthenticationApi,
        externalIdentityRepository = externalIdentityRepository,
    )

    @Test
    fun `검증된 연동 계정으로 가입하고 인증 토큰을 발급한다`() {
        val command = signupCommandFixture()
        val identity = verifiedExternalIdentityFixture()
        val memberCommand = slot<MemberRegisterCommand>()
        val externalIdentity = slot<ExternalIdentity>()
        val tokens = issuedAuthTokensFixture()
        every { identityTicketStore.consume(command.identityTicket) } returns identity
        every { memberRegistrationApi.register(capture(memberCommand)) } returns
                MemberRegisterResult(memberId = 42L, role = "MEMBER")
        every { externalIdentityRepository.save(capture(externalIdentity)) } answers { firstArg() }
        every { authTokenIssuer.issue(42L, "MEMBER") } returns tokens
        every {
            refreshTokenStore.save(42L, "refresh-token", TEST_REFRESH_TOKEN_TTL)
        } just Runs

        val result = authenticator.signup(command)

        result shouldBe AuthenticationResult(tokens, "MEMBER")
        memberCommand.captured shouldBe MemberRegisterCommand(
            nickname = "피카츄",
            createdAt = TEST_AUTHENTICATED_AT,
        )
        with(externalIdentity.captured) {
            provider shouldBe IdentityProvider.GOOGLE
            subject shouldBe "google-subject"
            memberId shouldBe 42L
            createdAt shouldBe TEST_AUTHENTICATED_AT
        }
        verifyOrder {
            identityTicketStore.consume("identity-ticket")
            memberRegistrationApi.register(memberCommand.captured)
            externalIdentityRepository.save(externalIdentity.captured)
            authTokenIssuer.issue(42L, "MEMBER")
            refreshTokenStore.save(42L, "refresh-token", TEST_REFRESH_TOKEN_TTL)
        }
    }

    @Test
    fun `유효하지 않은 신원 티켓으로 가입할 수 없다`() {
        every { identityTicketStore.consume("identity-ticket") } throws
                DomainException(AuthErrorCode.INVALID_IDENTITY_TICKET)

        val exception = shouldThrow<DomainException> {
            authenticator.signup(signupCommandFixture())
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_IDENTITY_TICKET
        verify(exactly = 0) {
            memberRegistrationApi.register(any())
            externalIdentityRepository.save(any())
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `가입한 연동 계정으로 로그인하고 인증 토큰을 발급한다`() {
        val identity = verifiedExternalIdentityFixture()
        val memberLoginCommand = slot<MemberLoginCommand>()
        val tokens = issuedAuthTokensFixture()
        every { identityTicketStore.consume("identity-ticket") } returns identity
        every {
            externalIdentityRepository.findByProviderAndSubject(identity.provider, identity.subject)
        } returns externalIdentityFixture(identity = identity)
        every { memberAuthenticationApi.login(capture(memberLoginCommand)) } returns
                MemberLoginResult("MEMBER")
        every { authTokenIssuer.issue(42L, "MEMBER") } returns tokens
        every {
            refreshTokenStore.save(42L, "refresh-token", TEST_REFRESH_TOKEN_TTL)
        } just Runs

        val result = authenticator.authenticate("identity-ticket")

        result shouldBe AuthenticationResult(tokens, "MEMBER")
        memberLoginCommand.captured shouldBe MemberLoginCommand(
            memberId = 42L,
            loggedInAt = TEST_AUTHENTICATED_AT,
        )
        verifyOrder {
            identityTicketStore.consume("identity-ticket")
            externalIdentityRepository.findByProviderAndSubject(
                IdentityProvider.GOOGLE,
                "google-subject",
            )
            memberAuthenticationApi.login(memberLoginCommand.captured)
            authTokenIssuer.issue(42L, "MEMBER")
            refreshTokenStore.save(42L, "refresh-token", TEST_REFRESH_TOKEN_TTL)
        }
    }

    @Test
    fun `가입하지 않은 연동 계정으로 로그인할 수 없다`() {
        val identity = verifiedExternalIdentityFixture().copy(subject = "unknown-subject")
        every { identityTicketStore.consume("identity-ticket") } returns identity
        every {
            externalIdentityRepository.findByProviderAndSubject(identity.provider, identity.subject)
        } returns null

        val exception = shouldThrow<DomainException> {
            authenticator.authenticate("identity-ticket")
        }

        exception.errorCode shouldBe AuthErrorCode.AUTHENTICATION_FAILED
        verify(exactly = 0) {
            memberAuthenticationApi.login(any())
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `연결 회원을 인증할 수 없으면 인증 실패로 변환한다`() {
        val identity = verifiedExternalIdentityFixture()
        every { identityTicketStore.consume("identity-ticket") } returns identity
        every {
            externalIdentityRepository.findByProviderAndSubject(identity.provider, identity.subject)
        } returns externalIdentityFixture(identity = identity)
        every { memberAuthenticationApi.login(any()) } throws
                DomainException(MemberErrorCode.MEMBER_NOT_FOUND)

        val exception = shouldThrow<DomainException> {
            authenticator.authenticate("identity-ticket")
        }

        exception.errorCode shouldBe AuthErrorCode.AUTHENTICATION_FAILED
        verify(exactly = 0) {
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰을 회전하고 인증 토큰을 다시 발급한다`() {
        val tokens = issuedAuthTokensFixture(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )
        every { refreshTokenStore.consume("refresh-token") } returns 42L
        every { memberAuthenticationApi.getMemberRole(42L) } returns MemberRoleResult("MEMBER")
        every { authTokenIssuer.issue(42L, "MEMBER") } returns tokens
        every {
            refreshTokenStore.save(42L, "new-refresh-token", TEST_REFRESH_TOKEN_TTL)
        } just Runs

        val result = authenticator.reissue("refresh-token")

        result shouldBe AuthenticationResult(tokens, "MEMBER")
        verifyOrder {
            refreshTokenStore.consume("refresh-token")
            memberAuthenticationApi.getMemberRole(42L)
            authTokenIssuer.issue(42L, "MEMBER")
            refreshTokenStore.save(42L, "new-refresh-token", TEST_REFRESH_TOKEN_TTL)
        }
    }

    @Test
    fun `유효하지 않은 리프레시 토큰으로 인증 토큰을 다시 발급할 수 없다`() {
        every { refreshTokenStore.consume("invalid-token") } throws
                DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        val exception = shouldThrow<DomainException> {
            authenticator.reissue("invalid-token")
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        verify(exactly = 0) {
            memberAuthenticationApi.getMemberRole(any())
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `연결 회원을 조회할 수 없으면 리프레시 토큰 오류로 변환한다`() {
        every { refreshTokenStore.consume("refresh-token") } returns 42L
        every { memberAuthenticationApi.getMemberRole(42L) } throws
                DomainException(MemberErrorCode.MEMBER_NOT_FOUND)

        val exception = shouldThrow<DomainException> {
            authenticator.reissue("refresh-token")
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        verify(exactly = 0) {
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰이 없으면 인증 토큰을 다시 발급할 수 없다`() {
        listOf<String?>(null, " ").forEach { refreshToken ->
            val exception = shouldThrow<DomainException> {
                authenticator.reissue(refreshToken)
            }

            exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        }
        verify(exactly = 0) {
            refreshTokenStore.consume(any())
            memberAuthenticationApi.getMemberRole(any())
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰을 삭제하고 로그아웃한다`() {
        every { refreshTokenStore.delete("refresh-token") } just Runs

        authenticator.logout("refresh-token")

        verify(exactly = 1) { refreshTokenStore.delete("refresh-token") }
    }

    @Test
    fun `리프레시 토큰이 없어도 로그아웃한다`() {
        authenticator.logout(null)
        authenticator.logout(" ")

        verify(exactly = 0) { refreshTokenStore.delete(any()) }
    }
}
