package com.zoonza.pokemoncardshop.auth.internal.application.port.`in`

import com.zoonza.pokemoncardshop.auth.internal.application.dto.IdentityTicketPurpose
import com.zoonza.pokemoncardshop.auth.internal.application.port.out.IdentityTicketPort
import com.zoonza.pokemoncardshop.auth.internal.application.service.IdentityTicketService
import com.zoonza.pokemoncardshop.auth.internal.domain.ExternalAccountRepository
import com.zoonza.pokemoncardshop.auth.test.fake.verifiedExternalIdentityFixture
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class IdentityTicketUseCaseTests {

    private val identityTicketPort = mockk<IdentityTicketPort>()
    private val externalAccountRepository = mockk<ExternalAccountRepository>()
    private val identityTicketUseCase: IdentityTicketUseCase = IdentityTicketService(
        identityTicketPort = identityTicketPort,
        externalAccountRepository = externalAccountRepository,
    )

    @Test
    fun `가입하지 않은 연동 계정에는 회원가입 티켓을 발급한다`() {
        val identity = verifiedExternalIdentityFixture().copy(subject = "new-subject")
        every {
            externalAccountRepository.existsByProviderAndSubject(identity.provider, identity.subject)
        } returns false
        every {
            identityTicketPort.issue(identity, IdentityTicketPurpose.SIGNUP.ttl)
        } returns "signup-ticket"

        val result = identityTicketUseCase.issue(identity)

        result.purpose shouldBe IdentityTicketPurpose.SIGNUP
        result.ticket shouldBe "signup-ticket"
        verify(exactly = 1) {
            externalAccountRepository.existsByProviderAndSubject(identity.provider, identity.subject)
            identityTicketPort.issue(identity, IdentityTicketPurpose.SIGNUP.ttl)
        }
    }

    @Test
    fun `가입한 연동 계정에는 로그인 티켓을 발급한다`() {
        val identity = verifiedExternalIdentityFixture().copy(subject = "registered-subject")
        every {
            externalAccountRepository.existsByProviderAndSubject(identity.provider, identity.subject)
        } returns true
        every {
            identityTicketPort.issue(identity, IdentityTicketPurpose.LOGIN.ttl)
        } returns "login-ticket"

        val result = identityTicketUseCase.issue(identity)

        result.purpose shouldBe IdentityTicketPurpose.LOGIN
        result.ticket shouldBe "login-ticket"
        verify(exactly = 1) {
            externalAccountRepository.existsByProviderAndSubject(identity.provider, identity.subject)
            identityTicketPort.issue(identity, IdentityTicketPurpose.LOGIN.ttl)
        }
    }
}
