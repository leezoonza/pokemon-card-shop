package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.SignupResult
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.AuthTokenPort
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketPort
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.RefreshTokenPort
import com.zoonza.pokemoncardshop.auth.internal.application.service.ExternalAccountCommandService
import com.zoonza.pokemoncardshop.auth.internal.domain.AuthErrorCode
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccount
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountProvider
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import com.zoonza.pokemoncardshop.auth.test.fake.*
import com.zoonza.pokemoncardshop.common.error.DomainException
import com.zoonza.pokemoncardshop.member.api.MemberRegisterCommand
import com.zoonza.pokemoncardshop.member.api.MemberRegisterResult
import com.zoonza.pokemoncardshop.member.api.MemberRegistrationApi
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.Test

class ExternalAccountCommandUseCaseTests {

    private val authTokenPort = mockk<AuthTokenPort>()
    private val refreshTokenPort = mockk<RefreshTokenPort>()
    private val identityTicketPort = mockk<IdentityTicketPort>()
    private val memberRegistrationApi = mockk<MemberRegistrationApi>()
    private val externalAccountRepository = mockk<ExternalAccountRepository>()
    private val externalAccountCommandUseCase: ExternalAccountCommandUseCase =
        ExternalAccountCommandService(
            authTokenPort = authTokenPort,
            refreshTokenPort = refreshTokenPort,
            identityTicketPort = identityTicketPort,
            memberRegistrationApi = memberRegistrationApi,
            externalAccountRepository = externalAccountRepository,
        )

    @Test
    fun `검증된 연동 계정으로 가입하고 인증 토큰을 발급한다`() {
        val command = signupCommandFixture()
        val identity = verifiedExternalIdentityFixture()
        val memberCommand = slot<MemberRegisterCommand>()
        val externalAccount = slot<ExternalAccount>()
        val tokens = issuedAuthTokensFixture()
        every { identityTicketPort.consume(command.identityTicket) } returns identity
        every { memberRegistrationApi.register(capture(memberCommand)) } returns
                MemberRegisterResult(memberId = 42L, role = "MEMBER")
        every { externalAccountRepository.save(capture(externalAccount)) } answers { firstArg() }
        every { authTokenPort.issue(42L, "MEMBER") } returns tokens
        every { refreshTokenPort.save(42L, "refresh-token", TEST_REFRESH_TOKEN_TTL) } just Runs

        val result = externalAccountCommandUseCase.signup(command)

        result shouldBe SignupResult(tokens, "MEMBER")
        memberCommand.captured shouldBe MemberRegisterCommand(
            nickname = "피카츄",
            createdAt = TEST_AUTHENTICATED_AT,
        )
        with(externalAccount.captured) {
            provider shouldBe ExternalAccountProvider.GOOGLE
            subject shouldBe "google-subject"
            memberId shouldBe 42L
            linkedAt shouldBe TEST_AUTHENTICATED_AT
        }
        verifyOrder {
            identityTicketPort.consume("identity-ticket")
            memberRegistrationApi.register(memberCommand.captured)
            externalAccountRepository.save(externalAccount.captured)
            authTokenPort.issue(42L, "MEMBER")
            refreshTokenPort.save(42L, "refresh-token", TEST_REFRESH_TOKEN_TTL)
        }
    }

    @Test
    fun `유효하지 않은 신원 티켓으로 가입할 수 없다`() {
        every { identityTicketPort.consume("identity-ticket") } throws
                DomainException(AuthErrorCode.INVALID_IDENTITY_TICKET)

        val exception = shouldThrow<DomainException> {
            externalAccountCommandUseCase.signup(signupCommandFixture())
        }

        exception.errorCode shouldBe AuthErrorCode.INVALID_IDENTITY_TICKET
        verify(exactly = 0) {
            memberRegistrationApi.register(any())
            externalAccountRepository.save(any())
            authTokenPort.issue(any(), any())
            refreshTokenPort.save(any(), any(), any())
        }
    }
}
