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
    private val memberRoleQueryApi = mockk<MemberRoleQueryApi>()
    private val externalIdentityRepository = mockk<ExternalIdentityRepository>()
    private val service = AuthService(
        clock,
        authTokenIssuer,
        refreshTokenStore,
        identityTicketStore,
        memberRegistrationApi,
        memberLoginApi,
        memberRoleQueryApi,
        externalIdentityRepository,
    )

    @Test
    fun `검증된 연동 계정으로 가입하고 인증 토큰을 발급한다`() {
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
    fun `가입한 연동 계정으로 로그인하고 인증 토큰을 발급한다`() {
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
    fun `가입하지 않은 연동 계정으로 로그인할 수 없다`() {
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

    @Test
    fun `연동 계정에 연결된 회원이 없으면 서버 오류로 처리한다`() {
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
        every { identityTicketStore.consume("identity-ticket") } returns verifiedIdentity
        every {
            externalIdentityRepository.findByProviderAndSubject(
                IdentityProvider.GOOGLE,
                "google-subject",
            )
        } returns externalIdentity
        every { memberLoginApi.recordLogin(any()) } returns null

        val exception = shouldThrow<IllegalStateException> {
            service.login("identity-ticket")
        }

        exception.message shouldBe "연동 계정에 연결된 회원 정보가 존재하지 않습니다."
        verify(exactly = 0) {
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰을 회전하고 인증 토큰을 다시 발급한다`() {
        val tokens = AuthTokens(
            accessToken = IssuedAccessToken("new-access-token"),
            refreshToken = IssuedRefreshToken("new-refresh-token", Duration.ofDays(14)),
        )

        every { refreshTokenStore.consume("refresh-token") } returns 42L
        every { memberRoleQueryApi.findByMemberId(42L) } returns MemberRoleResult("MEMBER")
        every { authTokenIssuer.issue(42L, "MEMBER") } returns tokens
        every {
            refreshTokenStore.save(42L, "new-refresh-token", Duration.ofDays(14))
        } just Runs

        val result = service.reissue("refresh-token")

        result shouldBe tokens
        verifyOrder {
            refreshTokenStore.consume("refresh-token")
            memberRoleQueryApi.findByMemberId(42L)
            authTokenIssuer.issue(42L, "MEMBER")
            refreshTokenStore.save(42L, "new-refresh-token", Duration.ofDays(14))
        }
    }

    @Test
    fun `유효하지 않은 리프레시 토큰으로 인증 토큰을 다시 발급할 수 없다`() {
        every { refreshTokenStore.consume("invalid-token") } throws
                DomainException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        val exception = shouldThrow<DomainException> {
            service.reissue("invalid-token")
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        verify(exactly = 1) { refreshTokenStore.consume("invalid-token") }
        verify(exactly = 0) {
            memberRoleQueryApi.findByMemberId(any())
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰의 회원이 존재하지 않으면 인증 토큰을 다시 발급할 수 없다`() {
        every { refreshTokenStore.consume("refresh-token") } returns 42L
        every { memberRoleQueryApi.findByMemberId(42L) } returns null

        val exception = shouldThrow<DomainException> {
            service.reissue("refresh-token")
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        verifyOrder {
            refreshTokenStore.consume("refresh-token")
            memberRoleQueryApi.findByMemberId(42L)
        }
        verify(exactly = 0) {
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰이 없으면 인증 토큰을 다시 발급할 수 없다`() {
        val exception = shouldThrow<DomainException> {
            service.reissue(null)
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        verify(exactly = 0) {
            refreshTokenStore.consume(any())
            memberRoleQueryApi.findByMemberId(any())
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `빈 리프레시 토큰으로 인증 토큰을 다시 발급할 수 없다`() {
        val exception = shouldThrow<DomainException> {
            service.reissue(" ")
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_REFRESH_TOKEN
        verify(exactly = 0) {
            refreshTokenStore.consume(any())
            memberRoleQueryApi.findByMemberId(any())
            authTokenIssuer.issue(any(), any())
            refreshTokenStore.save(any(), any(), any())
        }
    }

    @Test
    fun `리프레시 토큰을 삭제하고 로그아웃한다`() {
        every { refreshTokenStore.delete("refresh-token") } just Runs

        service.logout("refresh-token")

        verify(exactly = 1) { refreshTokenStore.delete("refresh-token") }
    }

    @Test
    fun `리프레시 토큰이 없어도 로그아웃한다`() {
        service.logout(null)

        verify(exactly = 0) { refreshTokenStore.delete(any()) }
    }

    @Test
    fun `빈 리프레시 토큰이어도 로그아웃한다`() {
        service.logout(" ")

        verify(exactly = 0) { refreshTokenStore.delete(any()) }
    }

    companion object {
        private val CREATED_AT: Instant = Instant.parse("2026-08-02T03:00:00Z")
    }
}
