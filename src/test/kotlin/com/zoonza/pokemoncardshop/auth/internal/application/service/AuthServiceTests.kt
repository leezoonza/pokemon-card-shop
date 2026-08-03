package com.zoonza.pokemoncardshop.auth.internal.application.service

import com.zoonza.pokemoncardshop.auth.internal.application.dto.*
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenIssuer
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketStore
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenStore
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentity
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalIdentityRepository
import com.zoonza.pokemoncardshop.auth.internal.domain.IdentityProvider
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.*
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class AuthServiceTests {

    private val clock = Clock.fixed(CREATED_AT, ZoneOffset.UTC)
    private val authTokenIssuer = mockk<AuthTokenIssuer>()
    private val refreshTokenStore = mockk<RefreshTokenStore>()
    private val identityTicketStore = mockk<IdentityTicketStore>()
    private val memberRegistrationApi = mockk<MemberRegistrationApi>()
    private val memberLoginApi = mockk<MemberLoginApi>()
    private val externalIdentityRepository = mockk<ExternalIdentityRepository>()
    private val service = AuthService(
        clock,
        authTokenIssuer,
        refreshTokenStore,
        identityTicketStore,
        memberRegistrationApi,
        memberLoginApi,
        externalIdentityRepository,
    )

    @Test
    fun `검증된 외부 신원으로 가입하고 인증 토큰을 발급한다`() {
        val command = SignupCommand("피카츄", "identity-ticket")
        val verifiedIdentity = VerifiedExternalIdentity(
            IdentityProvider.GOOGLE,
            "google-subject",
        )
        val memberCommand = slot<MemberRegisterCommand>()
        val externalIdentity = slot<ExternalIdentity>()
        val tokens = AuthTokens(
            accessToken = IssuedAccessToken("access-token"),
            refreshToken = IssuedRefreshToken("refresh-token", Duration.ofDays(14)),
        )

        every { identityTicketStore.consume(command.identityTicket) } returns verifiedIdentity
        every { memberRegistrationApi.register(capture(memberCommand)) } returns
            MemberRegisterResult(42L, "MEMBER")
        every { externalIdentityRepository.save(capture(externalIdentity)) } answers {
            firstArg()
        }
        every { authTokenIssuer.issue(42L, "MEMBER") } returns tokens
        every {
            refreshTokenStore.save(42L, "refresh-token", Duration.ofDays(14))
        } just Runs

        val result = service.signup(command)

        result shouldBe tokens

        memberCommand.captured.nickname shouldBe command.nickname
        memberCommand.captured.createdAt shouldBe CREATED_AT
        externalIdentity.captured.provider shouldBe IdentityProvider.GOOGLE
        externalIdentity.captured.subject shouldBe "google-subject"
        externalIdentity.captured.memberId shouldBe 42L
        externalIdentity.captured.createdAt shouldBe CREATED_AT

        verifyOrder {
            identityTicketStore.consume("identity-ticket")
            memberRegistrationApi.register(memberCommand.captured)
            externalIdentityRepository.save(externalIdentity.captured)
            authTokenIssuer.issue(42L, "MEMBER")
            refreshTokenStore.save(42L, "refresh-token", Duration.ofDays(14))
        }
    }

    @Test
    fun `유효하지 않은 신원 티켓으로 가입할 수 없다`() {
        val command = SignupCommand("피카츄", "invalid-ticket")

        every { identityTicketStore.consume(command.identityTicket) } throws
            DomainException(AuthErrorCode.INVALID_IDENTITY_TICKET)

        val exception = shouldThrow<DomainException> {
            service.signup(command)
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_IDENTITY_TICKET

        verify(exactly = 1) { identityTicketStore.consume("invalid-ticket") }
        verify(exactly = 0) {
            memberRegistrationApi.register(any())
            externalIdentityRepository.save(any())
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `가입한 외부 신원으로 로그인하고 인증 토큰을 발급한다`() {
        val verifiedIdentity = VerifiedExternalIdentity(
            IdentityProvider.GOOGLE,
            "google-subject",
        )
        val externalIdentity = ExternalIdentity.register(
            provider = IdentityProvider.GOOGLE,
            subject = "google-subject",
            memberId = 42L,
            createdAt = CREATED_AT.minusSeconds(60),
        )
        val memberLoginCommand = slot<MemberLoginCommand>()
        val tokens = AuthTokens(
            accessToken = IssuedAccessToken("access-token"),
            refreshToken = IssuedRefreshToken("refresh-token", Duration.ofDays(14)),
        )

        every { identityTicketStore.consume("identity-ticket") } returns verifiedIdentity
        every {
            externalIdentityRepository.findByProviderAndSubject(
                IdentityProvider.GOOGLE,
                "google-subject",
            )
        } returns externalIdentity
        every { memberLoginApi.recordLogin(capture(memberLoginCommand)) } returns
            MemberLoginResult("MEMBER")
        every { authTokenIssuer.issue(42L, "MEMBER") } returns tokens
        every {
            refreshTokenStore.save(42L, "refresh-token", Duration.ofDays(14))
        } just Runs

        val result = service.login("identity-ticket")

        result shouldBe tokens
        memberLoginCommand.captured.memberId shouldBe 42L
        memberLoginCommand.captured.loggedInAt shouldBe CREATED_AT

        verifyOrder {
            identityTicketStore.consume("identity-ticket")
            externalIdentityRepository.findByProviderAndSubject(
                IdentityProvider.GOOGLE,
                "google-subject",
            )
            memberLoginApi.recordLogin(memberLoginCommand.captured)
            authTokenIssuer.issue(42L, "MEMBER")
            refreshTokenStore.save(42L, "refresh-token", Duration.ofDays(14))
        }
    }

    @Test
    fun `가입하지 않은 외부 신원으로 로그인할 수 없다`() {
        val verifiedIdentity = VerifiedExternalIdentity(
            IdentityProvider.GOOGLE,
            "unknown-subject",
        )

        every { identityTicketStore.consume("identity-ticket") } returns verifiedIdentity
        every {
            externalIdentityRepository.findByProviderAndSubject(
                IdentityProvider.GOOGLE,
                "unknown-subject",
            )
        } returns null

        val exception = shouldThrow<DomainException> {
            service.login("identity-ticket")
        }

        exception.errorCode shouldBe AuthErrorCode.EXTERNAL_IDENTITY_NOT_FOUND
        verify(exactly = 0) {
            memberLoginApi.recordLogin(any())
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    companion object {
        private val CREATED_AT: Instant = Instant.parse("2026-08-02T03:00:00Z")
    }
}
